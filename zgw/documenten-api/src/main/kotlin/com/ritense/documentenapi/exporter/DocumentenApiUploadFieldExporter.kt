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

package com.ritense.documentenapi.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.deployment.ZgwDocumentCaseDefinitionUploadFields
import com.ritense.documentenapi.deployment.ZgwDocumentUploadField
import com.ritense.documentenapi.deployment.ZgwDocumentUploadFieldsChangeset
import com.ritense.documentenapi.repository.DocumentenApiUploadFieldRepository
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import mu.KotlinLogging
import java.time.Instant

class DocumentenApiUploadFieldExporter(
    private val documentenApiUploadFieldRepository: DocumentenApiUploadFieldRepository,
    private val objectMapper: ObjectMapper
) : Exporter<DocumentDefinitionExportRequest> {
    override fun supports(): Class<DocumentDefinitionExportRequest> = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        logger.info { "Exporting ZGW document list uploadFields for case definition ${request.name}" }
        val fields = documentenApiUploadFieldRepository.findAllByIdCaseDefinitionName(request.name)
            .map { ZgwDocumentUploadField(it.id.key, it.defaultValue, it.visible, it.readonly) }

        if (fields.isEmpty()) {
            return ExportResult(null)
        }

        val changeset = ZgwDocumentUploadFieldsChangeset(
            "${request.name}.zgw-document-upload-fields.${Instant.now().toEpochMilli()}",
            caseDefinitions = listOf(ZgwDocumentCaseDefinitionUploadFields(key = request.name, fields = fields))
        )

        return ExportResult(
            ExportFile(
                "config/case/zgw-document-upload-fields/${request.name}.zgw-document-upload-fields.json",
                objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(changeset)
            )
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}