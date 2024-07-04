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

package com.ritense.valtimo.export

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.exporter.request.DecisionDefinitionExportRequest
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream

@Transactional(readOnly = true)
class ProcessDefinitionExporterIntTest @Autowired constructor(
    private val repositoryService: RepositoryService,
    private val camundaRepositoryService:CamundaRepositoryService,
    private val processDefinitionExporter: ProcessDefinitionExporter
) : BaseIntegrationTest() {

    @Test
    fun `should export process definition with DMN reference`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "dmn-sample"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
        val result = processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val bpmnExportFile = result.exportFiles.singleOrNull {
            it.path == "bpmn/$processDefinitionKey.bpmn"
        }

        requireNotNull(bpmnExportFile)
        val bpmnModelInstance = ByteArrayInputStream(bpmnExportFile.content).use {
            Bpmn.readModelFromStream(it)
        }
        assertThat(bpmnModelInstance).isNotNull

        assertThat(result.relatedRequests).contains(
            DecisionDefinitionExportRequest(getDecisionDefinitionId("dmn-sample"))
        )

        assertThat(result.relatedRequests).contains(
            ProcessDefinitionExportRequest(getProcessDefinitionId("test-process"))
        )
    }

    @Test
    fun `should export process definition without DMN reference`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "test-process"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
        val result = processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val bpmnExportFile = result.exportFiles.singleOrNull {
            it.path == "bpmn/$processDefinitionKey.bpmn"
        }

        requireNotNull(bpmnExportFile)
        val bpmnModelInstance = ByteArrayInputStream(bpmnExportFile.content).use {
            Bpmn.readModelFromStream(it)
        }
        assertThat(bpmnModelInstance).isNotNull

        assertThat(result.relatedRequests).isEmpty()
    }

    @Test
    fun `should throw error when process definition contains an invalid DMN reference`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "invalid-dmn-ref"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
        val exception = assertThrows<IllegalStateException> {
            processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId))
        }

        assertThat(exception.message).isEqualTo("Decision definition with reference 'invalidDmnRef' could not be found!")
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