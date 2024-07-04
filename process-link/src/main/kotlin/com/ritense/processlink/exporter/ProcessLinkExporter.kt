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

package com.ritense.processlink.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.ExportRequest
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper
import com.ritense.valtimo.camunda.service.CamundaRepositoryService

class ProcessLinkExporter(
    private val objectMapper: ObjectMapper,
    private val processLinkService: ProcessLinkService,
    private val repositoryService: CamundaRepositoryService
) : Exporter<ProcessDefinitionExportRequest> {

    override fun supports(): Class<ProcessDefinitionExportRequest> = ProcessDefinitionExportRequest::class.java

    override fun export(request: ProcessDefinitionExportRequest): ExportResult {
        val processLinks = processLinkService.getProcessLinks(request.processDefinitionId)

        if (processLinks.isEmpty()) {
            return ExportResult()
        }

        val relatedRequests = mutableSetOf<ExportRequest>()
        val createDtos = processLinks.map { processLink ->
            val mapper = processLinkService.getProcessLinkMapper(processLink.processLinkType)

            relatedRequests.addAll(mapper.createRelatedExportRequests(processLink))

            mapper.toProcessLinkExportResponseDto(processLink)
        }

        val processDefinitionKey = getProcessDefinitionKey(request.processDefinitionId)

        return ExportResult(
            ExportFile(
                "config/processlink/${processDefinitionKey}.processlink.json",
                objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(createDtos)
            ),
            relatedRequests
        )
    }

    fun getProcessDefinitionKey(processDefinitionId: String): String {
        return requireNotNull(
            repositoryService.findProcessDefinition(
                CamundaProcessDefinitionSpecificationHelper.byId(processDefinitionId)
                    .and(CamundaProcessDefinitionSpecificationHelper.byLatestVersion())
            )
        ).key
    }
}