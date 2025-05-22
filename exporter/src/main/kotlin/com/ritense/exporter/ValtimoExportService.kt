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

package com.ritense.exporter

import com.ritense.exporter.request.ExportRequest
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ValtimoExportService (
    private val exporters: List<Exporter<ExportRequest>>
) : ExportService {

    override fun export(request: ExportRequest): ByteArrayOutputStream {
        val exportList: Set<ExportFile> = collectExportFiles(request)

        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zos ->
            exportList.forEach { exportFile ->
                val zipEntry = ZipEntry(exportFile.path)
                zos.putNextEntry(zipEntry)
                zos.write(exportFile.content)
                zos.closeEntry()
            }
        }
        return outputStream
    }

    private fun collectExportFiles(request: ExportRequest, history: MutableSet<ExportRequest> = mutableSetOf()): Set<ExportFile> {
        //This history prevents stack-overflows
        if (history.contains(request)) {
            return setOf()
        }
        history.add(request)

        return exporters.filter { exporter ->
            exporter.supports().isInstance(request)
        }.apply {
            if (isEmpty()) {
                logger.error { "No exporter found for export request of type '${request::class.java}'" }
            }
        }.mapNotNull { exporter ->
            try {
                val result = exporter.export(request)
                result.exportFiles + result.relatedRequests.flatMap {
                    collectExportFiles(it, history)
                }
            } catch (e: NoSuchElementException) {
                if (!request.required) {
                    null
                } else {
                    throw e
                }
            }
        }.flatten().toSet()
    }

    companion object {
        private val logger: mu.KLogger = mu.KotlinLogging.logger {}
    }
}