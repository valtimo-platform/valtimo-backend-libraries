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

package com.ritense.processdocument.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.processdocument.domain.config.ProcessDocumentLinkConfigItem
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService

class ProcessDocumentLinkExporter(
    private val objectMapper: ObjectMapper,
    private val camundaRepositoryService: CamundaRepositoryService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService
) : Exporter<DocumentDefinitionExportRequest> {

    override fun supports() = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        val exportItems = processDocumentAssociationService.findProcessDocumentDefinitions(
            request.name, null,null
        ).map { definition ->
            ProcessDocumentLinkConfigItem().apply {
                val processDefinitionKey = definition.processDocumentDefinitionId().processDefinitionKey().toString()
                this.processDefinitionKey = processDefinitionKey
                this.startableByUser = definition.startableByUser()
                this.canInitializeDocument = definition.canInitializeDocument()
            }
        }

        if (exportItems.isEmpty()) {
            return ExportResult()
        }

        val relatedRequests = exportItems.asSequence().map { it.processDefinitionKey }
            .distinct()
            .map { key -> requireNotNull(camundaRepositoryService.findLatestProcessDefinition(key)) }
            .map { processDefinition ->
                ProcessDefinitionExportRequest(processDefinition.id)
            }.toSet()

        return ExportResult(
            ExportFile(
                PATH.format(request.name),
                objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(exportItems)
            ),
            relatedRequests
        )
    }
    companion object {
        private const val PATH = "config/process-document-link/%s.json";
    }
}