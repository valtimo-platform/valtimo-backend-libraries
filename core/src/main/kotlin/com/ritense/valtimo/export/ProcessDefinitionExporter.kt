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

import com.ritense.export.ExportFile
import com.ritense.export.ExportResult
import com.ritense.export.Exporter
import com.ritense.export.request.DecisionDefinitionExportRequest
import com.ritense.export.request.ProcessDefinitionExportRequest
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import java.io.ByteArrayOutputStream
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask

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

        val decisionExportRequests = getDecisionExportRequests(bpmnModelInstance)

        val exportFile = ByteArrayOutputStream().use {
            Bpmn.writeModelToStream(it, bpmnModelInstance)
            ExportFile(
                "config/bpmn/${processDefinition.key}.bpmn",
                it.toByteArray()
            )
        }
        return ExportResult(
            exportFile,
            decisionExportRequests
        )
    }

    private fun getDecisionExportRequests(bpmnModelInstance: BpmnModelInstance): Set<DecisionDefinitionExportRequest> {
        return bpmnModelInstance.getModelElementsByType(BusinessRuleTask::class.java)
            .mapNotNull { it.camundaDecisionRef }
            .distinct()
            .map { ref ->
                val decisionDefinition = checkNotNull(repositoryService.createDecisionDefinitionQuery()
                    .decisionDefinitionKey(ref)
                    .latestVersion()
                    .singleResult()) { "Decision definition with reference '$ref' could not be found!"}
                DecisionDefinitionExportRequest(decisionDefinition.id)
            }.toSet()
    }
}