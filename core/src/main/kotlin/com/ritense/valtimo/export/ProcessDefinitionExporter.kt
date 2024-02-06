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

import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DecisionDefinitionExportRequest
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask
import org.camunda.bpm.model.bpmn.instance.CallActivity
import java.io.ByteArrayOutputStream

class ProcessDefinitionExporter(
    private val camundaRepositoryService: CamundaRepositoryService,
    private val repositoryService: RepositoryService,
) : Exporter<ProcessDefinitionExportRequest> {
    override fun supports(): Class<ProcessDefinitionExportRequest> = ProcessDefinitionExportRequest::class.java

    override fun export(request: ProcessDefinitionExportRequest): ExportResult {
        val processDefinition = requireNotNull(
            camundaRepositoryService.findProcessDefinitionById(request.processDefinitionId)
        )

        val bpmnModelInstance = repositoryService.getProcessModel(processDefinition.id).use { inputStream ->
            Bpmn.readModelFromStream(inputStream)
        }

        val subProcessDefinitionExportRequests = getCallActivityProcessDefinitionExportRequests(bpmnModelInstance)
        val decisionExportRequests = getDecisionExportRequests(bpmnModelInstance)

        val exportFile = ByteArrayOutputStream().use {
            Bpmn.writeModelToStream(it, bpmnModelInstance)
            ExportFile(
                "bpmn/${processDefinition.key}.bpmn",
                it.toByteArray()
            )
        }
        return ExportResult(
            exportFile,
            subProcessDefinitionExportRequests + decisionExportRequests
        )
    }

    private fun getCallActivityProcessDefinitionExportRequests(bpmnModelInstance: BpmnModelInstance): Set<ProcessDefinitionExportRequest> {
        return bpmnModelInstance.getModelElementsByType(CallActivity::class.java)
            .mapNotNull { it.calledElement }
            .distinct()
            .map { key ->
                val processDefinitionId = checkNotNull(camundaRepositoryService.findLatestProcessDefinition(key)) {
                    "Process definition with key '$key' could not be found!"
                }.id
                ProcessDefinitionExportRequest(processDefinitionId)
            }.toSet()
    }

    private fun getDecisionExportRequests(bpmnModelInstance: BpmnModelInstance): Set<DecisionDefinitionExportRequest> {
        return bpmnModelInstance.getModelElementsByType(BusinessRuleTask::class.java)
            .mapNotNull { it.camundaDecisionRef }
            .distinct()
            .map { ref ->
                val decisionDefinition = checkNotNull(repositoryService.createDecisionDefinitionQuery()
                    .decisionDefinitionKey(ref)
                    .latestVersion()
                    .singleResult()) { "Decision definition with reference '$ref' could not be found!" }
                DecisionDefinitionExportRequest(decisionDefinition.id)
            }.toSet()
    }
}