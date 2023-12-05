/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.import

import java.io.InputStream
import java.util.zip.ZipInputStream
import mu.KLogger
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

open class ValtimoImportService(
    importers: Set<Importer>
) : ImportService {

    private val orderedImporters: LinkedHashSet<Importer>

    init {
        //Check for duplicate import types in the list of importers
        val wImporters = importers.map { WrappedImporter(it) }.toSet()
        if (wImporters.size != importers.size) {
            val duplicatedTypes = wImporters.filter { wImporter ->
                importers.count { wImporter.type() == it.type() } > 1
            }.map { it.type() }
            throw ImportServiceException(
                "Multiple importers of the same type provided: [${duplicatedTypes.joinToString()}]"
            )
        }

        // Order and validate importers by their dependencies
        val orderedImporters = LinkedHashMap<String, Importer>()
        while (orderedImporters.size < importers.size) {
            importers.filter {
                !orderedImporters.containsKey(it.type()) &&
                    orderedImporters.keys.containsAll(it.dependsOn())
            }.apply {
                if (this.isEmpty()) {
                    throw ImportServiceException(
                        "Importer dependencies could not be resolved or contain a cyclic reference! " +
                            "Error occurred after: [${orderedImporters.keys.joinToString()}]"
                    )
                }
            }.forEach {
                orderedImporters[it.type()] = it
            }
        }
        this.orderedImporters = linkedSetOf(*orderedImporters.values.toTypedArray())
    }

    @Transactional
    override fun import(inputStream: InputStream) {
        val entries = readZipEntries(inputStream)
        val importerEntriesMap = getEntriesByImporter(entries)

        val handledTypes = mutableSetOf<String>()
        importerEntriesMap.forEach { (importer, entries) ->
            if (!handledTypes.containsAll(importer.dependsOn())) {
                throw ImportServiceException(
                    "Could not import files of type ${importer.type()}! " +
                        "The dependencies (${importer.dependsOn().joinToString()}) were not fulfilled. " +
                        "Failed after types: ${handledTypes.joinToString()}"
                )

            }

            entries.forEach {
                importer.import(ImportRequest(it.content))
            }
            handledTypes.add(importer.type())
        }
    }

    private fun readZipEntries(inputStream: InputStream): List<ZipFileEntry> {
        // Read all entries with data from the stream
        return try {
            ZipInputStream(inputStream).use { stream ->
                generateSequence { stream.nextEntry }
                    .filter { !it.isDirectory }
                    .map { ZipFileEntry(it.name, stream.readBytes()) }
                    .toMutableList()
            }
        } catch (ex: Exception) {
            throw ImportServiceException(ex.message)
        }
    }

    /**
     * Maps all entries by a supporting importer.
     * When no files are provided, an empty list value is mapped.
     * @param entries
     */
    private fun getEntriesByImporter(entries: List<ZipFileEntry>): LinkedHashMap<Importer, List<ZipFileEntry>> {
        val entryPairs = entries.mapNotNull { entry ->
            orderedImporters.filter { importer ->
                importer.supports(entry.fileName)
            }.apply {
                if(this.isEmpty()) {
                    logger.info { "No importer candidate found for file ${entry.fileName}." }
                } else if (this.size > 1) {
                    throw ImportServiceException(
                        "Multiple importer candidates found for file ${entry.fileName}! " +
                            "Importer types: [${this.joinToString { it.type() }}]"
                    )
                }
            }.firstOrNull()?.let {
                Pair(it, entry)
            }
        }

        // The map keys are kept in the same order as `importers` by using a LinkedHashMap
        return orderedImporters.associateWithTo(LinkedHashMap()) { importer ->
            entryPairs.filter {
                it.first == importer
            }.map {
                it.second
            }
        }
    }

    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
        class WrappedImporter(

            private val importer: Importer
        ) : Importer by importer {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Importer

                return importer.type() == other.type()
            }

            override fun hashCode(): Int {
                return importer.type().hashCode()
            }
        }

        data class ZipFileEntry(
            val fileName: String,
            val content: ByteArray
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ZipFileEntry

                return fileName == other.fileName
            }

            override fun hashCode(): Int {
                return fileName.hashCode()
            }
        }
    }
}