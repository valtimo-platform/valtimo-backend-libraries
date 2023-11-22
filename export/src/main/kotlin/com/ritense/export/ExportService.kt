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

package com.ritense.export

import com.ritense.export.request.ExportRequest
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

open class ExportService (
    private val exporters: List<Exporter<ExportRequest>>
) {

    open fun export(request: ExportRequest): ByteArrayOutputStream {
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

    open fun collectExportFiles(request: ExportRequest): Set<ExportFile> {
        val exportList: Set<ExportFile> = exporters.filter { exporter ->
            exporter.supports().isInstance(request)
        }.flatMap { exporter ->
            exporter.export(request)
        }.toSet()
        return exportList
    }
}