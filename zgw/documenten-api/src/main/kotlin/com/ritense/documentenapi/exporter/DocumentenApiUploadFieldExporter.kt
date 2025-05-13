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
import com.ritense.documentenapi.deployment.ZgwDocumentUploadField
import com.ritense.documentenapi.repository.DocumentenApiUploadFieldRepository
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import mu.KotlinLogging

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
            return ExportResult()
        }

        val formattedCaseDefinitionVersion = request.caseDefinitionId.versionTag.let {
            "${it.major}-${it.minor}-${it.patch}"
        }

        return ExportResult(
            ExportFile(
                PATH.format(request.caseDefinitionId.key, formattedCaseDefinitionVersion, request.name),
                objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(fields)
            )
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val PATH = "config/case/%s/%s/zgw/document-upload-fields/%s.zgw-document-upload-field.json"
    }
}