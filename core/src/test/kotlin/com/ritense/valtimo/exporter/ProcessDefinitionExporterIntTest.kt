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

package com.ritense.valtimo.exporter

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.exporter.request.DecisionDefinitionExportRequest
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.service.CamundaProcessService
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.DecisionDefinition
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream

@Transactional
class ProcessDefinitionExporterIntTest @Autowired constructor(
    private val repositoryService: RepositoryService,
    private val camundaRepositoryService: CamundaRepositoryService,
    private val processDefinitionExporter: ProcessDefinitionExporter,
    private val camundaProcessService: CamundaProcessService,
) : BaseIntegrationTest() {

    @Test
    fun `should export process definition with DMN reference`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "dmn-sample"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
        val caseDefinitionId = CaseDefinitionId("everything", "1.0.0")
        val result =
            processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId, caseDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val bpmnExportFile = result.exportFiles.singleOrNull {
            it.path == "config/case/everything/1-0-0/bpmn/$processDefinitionKey.bpmn"
        }

        requireNotNull(bpmnExportFile)
        val bpmnModelInstance = ByteArrayInputStream(bpmnExportFile.content).use {
            Bpmn.readModelFromStream(it)
        }
        assertThat(bpmnModelInstance).isNotNull

        assertThat(result.relatedRequests).contains(
            DecisionDefinitionExportRequest(
                getDecisionDefinitionId("dmn-sample"),
                CaseDefinitionId.of("everything", "1.0.0")
            )
        )
    }

    @Test
    fun `should export process definition with reference to latest DMN`(): Unit = runWithoutAuthorization {
        val firstDecisionDefinitionId = getDecisionDefinitionId("dmn-sample")
        val processDefinitionKey = "dmn-sample"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
        val caseDefinitionId = CaseDefinitionId("everything", "1.0.0")
        val newDmnString = getFileAsString("config/case/everything/1-0-0/dmn/dmn-sample.dmn")
            .replace("name=\"Decision 1\"", "name=\"Sample Decision\"")
        val deployment = camundaProcessService.deploy(caseDefinitionId, "dmn-sample.dmn", newDmnString.byteInputStream())
        val secondDecisionDefinitionId = deployment.deployedDecisionDefinitions.first().id

        val result =
            processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId, caseDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()
        assertThat(firstDecisionDefinitionId).isNotEqualTo(secondDecisionDefinitionId)
        assertThat(result.relatedRequests).contains(
            DecisionDefinitionExportRequest(
                secondDecisionDefinitionId,
                CaseDefinitionId.of("everything", "1.0.0")
            )
        )
    }

    @Test
    fun `should export process definition without DMN reference`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "test-process"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
        val caseDefinitionId = CaseDefinitionId("everything", "1.0.0")
        val result =
            processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId, caseDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val bpmnExportFile = result.exportFiles.singleOrNull {
            it.path == "config/case/everything/1-0-0/bpmn/$processDefinitionKey.bpmn"
        }

        requireNotNull(bpmnExportFile)
        val bpmnModelInstance = ByteArrayInputStream(bpmnExportFile.content).use {
            Bpmn.readModelFromStream(it)
        }
        assertThat(bpmnModelInstance).isNotNull

        assertThat(result.relatedRequests).isEmpty()
    }

    @Test
    fun `should throw error when process definition contains an invalid DMN reference`(): Unit =
        runWithoutAuthorization {
            val processDefinitionKey = "invalid-dmn-ref"
            val processDefinitionId = getProcessDefinitionId(processDefinitionKey)
            val caseDefinitionId = CaseDefinitionId("invalid-dmn-ref", "1.0.0")
            val exception = assertThrows<IllegalStateException> {
                processDefinitionExporter.export(ProcessDefinitionExportRequest(processDefinitionId, caseDefinitionId))
            }

            assertThat(exception.message).isEqualTo("Decision definition with reference 'invalidDmnRef' could not be found!")
        }

    private fun getProcessDefinitionId(processDefinitionKey: String): String {
        return requireNotNull(
            camundaRepositoryService.findProcessDefinition(
                byKey(processDefinitionKey)
                    .and(byLatestVersion())
            )
        ).id
    }

    private fun getDecisionDefinition(decisionDefinitionKey: String): DecisionDefinition {
        return repositoryService.createDecisionDefinitionQuery()
            .decisionDefinitionKey(decisionDefinitionKey)
            .latestVersion()
            .singleResult()
    }

    private fun getDecisionDefinitionId(decisionDefinitionKey: String): String {
        return getDecisionDefinition(decisionDefinitionKey).id
    }
}