/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.importer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.importer.ImportContext.Companion.runImporter
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.exception.CyclicImporterDependencyException
import com.ritense.importer.exception.DuplicateImporterTypeException
import com.ritense.importer.exception.InvalidImportZipException
import com.ritense.importer.exception.TooManyImportCandidatesException
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.util.zip.ZipInputStream

open class ValtimoImportService(
    importers: Set<Importer>,
    private val environment: Environment,
    val whitelistedEnvironmentProperties: List<Regex>
) : ImportService {

    private val orderedImporters = distinctImporters(importers).let {
        filterImportersByDependsOn(it)
    }.let {
        orderImporters(it)
    }

    /**
     * Get a distinct set of importers by type.
     * Fails when duplicates are found.
     */
    private fun distinctImporters(importers: Set<Importer>): Set<Importer> {
        return importers.map { WrappedImporter(it) }
            .toSet()
            .apply {
                if (this.size != importers.size) {
                    val duplicatedTypes = this.filter { wImporter ->
                        importers.count { wImporter.type() == it.type() } > 1
                    }.map { it.type() }.toSet()
                    throw DuplicateImporterTypeException(duplicatedTypes)
                }
            }
    }

    /**
     * This will filter out any importer that depends on an importer that is not provided.
     */
    private fun filterImportersByDependsOn(importers: Set<Importer>): Set<Importer> {
        var result = importers

        while (result.isNotEmpty()) {
            //Filter out importers of which any of the dependencies cannot be resolved
            val filtered = result.filter { importer ->
                importer.dependsOn().all { type ->
                    result.any { it.type() == type }
                        .also { dependencyFound ->
                            if (!dependencyFound) {
                                logger.warn { "Importer ${importer.type()} depends on '$type', which cannot be resolved. Importer will not be used!" }
                            }
                        }
                }
            }.toSet()

            // Check if any importer was filtered. If not, we can stop the loop
            if (filtered.size == result.size) {
                break
            }

            result = filtered
        }

        return result
    }

    /**
     * Order the imports by their dependencies.
     * Fail when the dependencies form a circular dependency
     */
    private fun orderImporters(importers: Set<Importer>): LinkedHashSet<Importer> {
        val orderedImporters = LinkedHashMap<String, Importer>()
        while (orderedImporters.size < importers.size) {
            importers.filter {
                !orderedImporters.containsKey(it.type())
                    && orderedImporters.keys.containsAll(it.dependsOn())
            }.apply {
                if (this.isEmpty()) {
                    throw CyclicImporterDependencyException(orderedImporters.keys)
                }
            }.forEach {
                orderedImporters[it.type()] = it
            }
        }
        return linkedSetOf(*orderedImporters.values.toTypedArray())
    }

    @Transactional
    open fun importCaseDefinition(
        resources: List<Pair<String, Resource>>,
        caseDefinitionIdList: List<CaseDefinitionId>
    ) {
        runImporter {
            // If case definition is already imported, don't import the rest of the files for the case definition
            // (so a skip)

            val importerEntriesList = getEntriesByImporter(getEntriesFromResources(resources))
            val caseDefinitionId: CaseDefinitionId?
            val caseDefinitionEntries = importerEntriesList
                .filter { it.key.type() == CASE_DEFINITION }
                .let {
                    it[it.keys.first()]
                }
            val caseDefinitionContent = caseDefinitionEntries?.firstOrNull()?.content ?:
                throw IllegalStateException("No case definition file found in the provided resources")
            val caseDefinitionMap: Map<String, Any> = jacksonObjectMapper()
                .readValue(caseDefinitionContent)
            caseDefinitionId = CaseDefinitionId(
                caseDefinitionMap["key"] as String,
                caseDefinitionMap["versionTag"] as String
            )

            if (caseDefinitionIdList.contains(caseDefinitionId)) {
                return@runImporter
            }

            importerEntriesList.filter { it.key.partOfCaseDefinition() }.forEach { (importer, entries) ->
                entries.forEach { entry ->
                    logger.debug { "Importing ${entry.fileName} with importer ${importer.type()}" }
                    importer.import(ImportRequest(entry.fileName, entry.content, caseDefinitionId))
                }
            }

            importerEntriesList.filter { it.key.partOfCaseDefinition() }.forEach { (importer, entries) ->
                entries.forEach { entry ->
                    importer.afterImport(ImportRequest(entry.fileName, entry.content, caseDefinitionId))
                }
            }
        }

    }

    @Transactional
    open fun importGlobalDefinitions(
        resources: List<Pair<String, Resource>>
    ) {
        runImporter {
           val importerEntriesList = getEntriesByImporter(getEntriesFromResources(resources))

            importerEntriesList.filter { !it.key.partOfCaseDefinition() }.forEach { (importer, entries) ->
                entries.forEach { entry ->
                    logger.debug { "Importing ${entry.fileName} with importer ${importer.type()}" }
                    importer.import(ImportRequest(entry.fileName, entry.content))
                }
            }
            importerEntriesList.filter { !it.key.partOfCaseDefinition() }.forEach { (importer, entries) ->
                entries.forEach { entry ->
                    importer.afterImport(ImportRequest(entry.fileName, entry.content))
                }
            }
        }

    }

    @Transactional
    override fun import(inputStream: InputStream, caseDefinitionIdList: List<CaseDefinitionId>) {
        runImporter {
            val entries = readZipEntries(inputStream)
            val importerEntriesList = getEntriesByImporter(entries).ifEmpty { return@runImporter }
            val caseDefinitionId: CaseDefinitionId?
            val filteredImporterEntriesList = importerEntriesList
                .filter { it.key.type() == CASE_DEFINITION }

            if (filteredImporterEntriesList.isNotEmpty()) {
                val caseDefinitionEntries = filteredImporterEntriesList.let {
                    it[it.keys.first()]
                }
                val caseDefinitionMap: Map<String, Any> = jacksonObjectMapper()
                    .readValue(caseDefinitionEntries?.first()?.content!!) // TODO: Throw proper error message
                caseDefinitionId = CaseDefinitionId(
                    caseDefinitionMap["key"] as String,
                    caseDefinitionMap["versionTag"] as String
                )

                if (caseDefinitionIdList.contains(caseDefinitionId)) {
                    return@runImporter
                }

                importerEntriesList.filter { it.key.partOfCaseDefinition() }.forEach { (importer, entries) ->
                    entries.forEach { entry ->
                        logger.debug { "Importing ${entry.fileName} with importer ${importer.type()}" }
                        importer.import(ImportRequest(entry.fileName, entry.content, caseDefinitionId))
                    }
                }

            }

            importerEntriesList.filter { !it.key.partOfCaseDefinition() }.forEach { (importer, entries) ->
                entries.forEach { entry ->
                    logger.debug { "Importing ${entry.fileName} with importer ${importer.type()}" }
                    importer.import(ImportRequest(entry.fileName, entry.content))
                }
            }

            importerEntriesList.forEach { (importer, entries) ->
                entries.forEach { entry ->
                    importer.afterImport(ImportRequest(entry.fileName, entry.content))
                }
            }
        }
    }

    private fun readZipEntries(inputStream: InputStream): List<ZipFileEntry> {
        // Read all entries with data from the stream
        return try {
            ZipInputStream(inputStream).use { stream ->
                generateSequence { stream.nextEntry }
                    .filter { !it.isDirectory }
                    .map { ZipFileEntry(prepareFilePath(it.name), stream.readBytes()) }
                    .toMutableList()
            }
        } catch (ex: Exception) {
            throw InvalidImportZipException(ex.message)
        }.apply {
            if (this.isEmpty()) {
                throw InvalidImportZipException("Archive was empty or not a zip")
            }
        }
    }

    private fun prepareFilePath(path: String): String {
        return if (path.startsWith("config/case")) {
            val relativePath = path.substringAfter("config/case")
            val subStringStartIndex = StringUtils.ordinalIndexOf(relativePath, "/", 3)
            if (subStringStartIndex > -1) {
                relativePath.substring(subStringStartIndex)
            } else {
                path
            }
        } else {
            path
        }
    }

    private fun getEntriesFromResources(resources: List<Pair<String, Resource>>): List<ZipFileEntry> {
        return resources.map {
            val resolvedContent = resolveProperties(it.second.getContentAsString(Charsets.UTF_8))

            ZipFileEntry(it.first, resolvedContent.toByteArray(Charsets.UTF_8))
        }
    }

    private fun resolveProperties(content: String): String {
        var resolvedContent = content
        Regex("\\$\\{([^\\}]+)\\}").findAll(content)
            .map { it.groupValues }
            .forEach { (placeholder, placeholderValue) ->
                try {
                    whitelistedEnvironmentProperties.firstOrNull { it.matches(placeholderValue) }?.let {
                        val resolvedValue = environment.getProperty(placeholderValue)
                        if (!resolvedValue.isNullOrBlank()) {
                            resolvedContent = resolvedContent.replace(placeholder, resolvedValue)
                        }
                    }
                } catch (e: Exception) {
                    // ignored
                }
            }
        return resolvedContent
    }

    // change entries to be nested
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
                if (this.isEmpty()) {
                    logger.info { "No importer candidate found for file ${entry.fileName}." }
                } else if (this.size > 1) {
                    throw TooManyImportCandidatesException(
                        entry.fileName,
                        this.map { it.type() }.toSet()
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
        val logger = KotlinLogging.logger {}

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