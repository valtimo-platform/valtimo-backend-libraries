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
import com.ritense.documentenapi.deployment.ZgwDocumentListColumn
import com.ritense.documentenapi.deployment.ZgwDocumentListColumnChangeset
import com.ritense.documentenapi.deployment.ZgwDocumentListColumnCollection
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import mu.KotlinLogging
import java.time.Instant

class ZgwDocumentListColumnExporter(
    private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    private val objectMapper: ObjectMapper
) : Exporter<DocumentDefinitionExportRequest> {
    override fun supports(): Class<DocumentDefinitionExportRequest> = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        logger.info { "Exporting ZGW document list columns for case definition ${request.name}" }
        val columns = documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(request.name)
            .map { ZgwDocumentListColumn(it.id.key, it.defaultSort) }

        if (columns.isEmpty()) {
            return ExportResult(null)
        }

        val changeset = ZgwDocumentListColumnChangeset(
            "${request.name}.zgw-document-list-column.${Instant.now().toEpochMilli()}",
            caseDefinitions = listOf(ZgwDocumentListColumnCollection(key = request.name, columns = columns))
        )

        return ExportResult(
            ExportFile(
                "config/case/zgw-document-list-columns/${request.name}.zgw-document-list-column.json",
                objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(changeset)
            )
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}