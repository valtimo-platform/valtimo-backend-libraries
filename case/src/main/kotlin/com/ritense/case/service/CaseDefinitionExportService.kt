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

package com.ritense.case.service

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.JsonSchemaDocumentDefinitionExportService
import com.ritense.valtimo.contract.domain.ExportFile
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

open class CaseDefinitionExportService(
    private val documentDefinitionExportService: JsonSchemaDocumentDefinitionExportService
) {
    open fun createExport(caseDefinitionId: JsonSchemaDocumentDefinitionId): ByteArrayOutputStream {
        val exportList: Set<ExportFile> = documentDefinitionExportService.export(caseDefinitionId)
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            exportList.forEach { exportFile ->
                val zipEntry = ZipEntry(exportFile.path)
                zos.putNextEntry(zipEntry)
                zos.write(exportFile.content)
                zos.closeEntry()
            }
        }
        return baos
    }
}