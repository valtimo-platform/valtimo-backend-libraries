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
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.autodeployment.ProcessLinkDeploymentApplicationReadyEventListener
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class ProcessLinkExporterIntTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val camundaRepositoryService: CamundaRepositoryService,
    private val processLinkExporter: ProcessLinkExporter,
    private val listener: ProcessLinkDeploymentApplicationReadyEventListener
) : BaseIntegrationTest() {


    @BeforeEach
    fun before() {
        listener.deployProcessLinks()
    }

    @Test
    fun `should export process links`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "auto-deploy-process-link-with-long-key"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)

        val result = processLinkExporter.export(ProcessDefinitionExportRequest(processDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val exportFile = result.exportFiles.single {
            it.path == "config/processlink/auto-deploy-process-link-with-long-key.processlink.json"
        }

        val createRequestDtos: List<ProcessLinkExportResponseDto> = objectMapper.readValue(exportFile.content)
        assertThat(createRequestDtos).isNotEmpty

        assertThat(result.relatedRequests).contains(CustomProcessLinkNestedExportRequest())
    }

    fun getProcessDefinitionId(processDefinitionKey: String): String {
        return requireNotNull(
            camundaRepositoryService.findProcessDefinition(
                CamundaProcessDefinitionSpecificationHelper.byKey(processDefinitionKey)
                    .and(CamundaProcessDefinitionSpecificationHelper.byLatestVersion())
            )
        ).id
    }
}