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

package com.ritense.valtimo.export

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.export.request.DecisionDefinitionExportRequest
import com.ritense.export.request.ProcessDefinitionExportRequest
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import java.io.ByteArrayInputStream
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class ProcessDefinitionExporterIntTest @Autowired constructor(
    private val repositoryService: RepositoryService,
    private val camundaRepositoryService:CamundaRepositoryService,
    private val processDefinitionExporter: ProcessDefinitionExporter
) : BaseIntegrationTest() {

    @Test
    fun `should export process definition`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "dmn-sample"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
        val result = processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val bpmnExportFile = result.exportFiles.singleOrNull {
            it.path == "config/bpmn/$processDefinitionKey.bpmn"
        }

        requireNotNull(bpmnExportFile)
        val bpmnModelInstance = ByteArrayInputStream(bpmnExportFile.content).use {
            Bpmn.readModelFromStream(it)
        }
        assertThat(bpmnModelInstance).isNotNull

        val decisionExportRequest = result.relatedRequests
            .singleOrNull {
                it is DecisionDefinitionExportRequest && it.decisionDefinitionId == getDecisionDefinitionId("dmn-sample")
            }
        assertThat(decisionExportRequest).isNotNull
    }

    fun getProcessDefinitionId(processDefinitionKey: String): String {
        return requireNotNull(
            camundaRepositoryService.findProcessDefinition(
                byKey(processDefinitionKey)
                    .and(byLatestVersion()))
        ).id
    }

    fun getDecisionDefinitionId(decisionDefinitionKey:String): String {
        return repositoryService.createDecisionDefinitionQuery()
            .decisionDefinitionKey(decisionDefinitionKey)
            .latestVersion()
            .singleResult()
            .id
    }
}