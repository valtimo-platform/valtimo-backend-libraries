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

import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DecisionDefinitionExportRequest
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.valtimo.camunda.repository.CamundaDecisionDefinitionSpecificationHelper
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byVersion
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byVersionTag
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
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

        val formattedCaseDefinitionVersion = request.caseDefinitionId.versionTag.let {
            "${it.major}-${it.minor}-${it.patch}"
        }

        val subProcessDefinitionExportRequests = getCallActivityProcessDefinitionExportRequests(bpmnModelInstance, request.caseDefinitionId)
        val decisionExportRequests = getDecisionExportRequests(request.caseDefinitionId, bpmnModelInstance)

        val exportFile = ByteArrayOutputStream().use {
            Bpmn.writeModelToStream(it, bpmnModelInstance)
            ExportFile(
                PATH.format(request.caseDefinitionId.key, formattedCaseDefinitionVersion, processDefinition.key),
                it.toByteArray()
            )
        }
        return ExportResult(
            exportFile,
            subProcessDefinitionExportRequests + decisionExportRequests
        )
    }

    private fun getCallActivityProcessDefinitionExportRequests(bpmnModelInstance: BpmnModelInstance, caseDefinitionId: CaseDefinitionId): Set<ProcessDefinitionExportRequest> {
        return bpmnModelInstance.getModelElementsByType(CallActivity::class.java).mapNotNull {
            if (it.calledElement != null) {
                val spec = byKey(it.calledElement)
                val processDefinitionId = checkNotNull(
                    when (it.camundaCalledElementBinding) {
                        "version" -> camundaRepositoryService.findProcessDefinition(spec.and(byVersion(it.camundaCalledElementVersion.toInt())))
                        "versionTag" -> camundaRepositoryService.findProcessDefinition(spec.and(byVersionTag(it.camundaCalledElementVersionTag)))
                        "deployment" -> null
                        else -> camundaRepositoryService.findProcessDefinition(spec.and(byLatestVersion()))
                    }
                ) {
                    "Process definition with key '${it.calledElement}' could not be found!"
                }.id
                ProcessDefinitionExportRequest(processDefinitionId, caseDefinitionId)
            } else {
                null
            }
        }.toSet()
    }

    private fun getDecisionExportRequests(caseDefinitionId: CaseDefinitionId, bpmnModelInstance: BpmnModelInstance): Set<DecisionDefinitionExportRequest> {
        return bpmnModelInstance.getModelElementsByType(BusinessRuleTask::class.java).mapNotNull {
            if (it.camundaDecisionRef != null) {
                val spec = CamundaDecisionDefinitionSpecificationHelper.byKey(it.camundaDecisionRef)
                val decisionDefinitionId = checkNotNull(
                    when (it.camundaDecisionRefBinding) {
                        "version" -> camundaRepositoryService.findDecisionDefinition(spec.and(CamundaDecisionDefinitionSpecificationHelper.byVersion(it.camundaDecisionRefVersion.toInt())))
                        "versionTag" -> camundaRepositoryService.findDecisionDefinition(spec.and(CamundaDecisionDefinitionSpecificationHelper.byVersionTag(it.camundaDecisionRefVersionTag)))
                        "deployment" -> null
                        else -> camundaRepositoryService.findDecisionDefinition(spec.and(CamundaDecisionDefinitionSpecificationHelper.byLatestVersion()))
                    }
                ) {
                    "Decision definition with reference '${it.camundaDecisionRef}' could not be found!"
                }.id
                DecisionDefinitionExportRequest(decisionDefinitionId, caseDefinitionId)
            } else {
                null
            }
        }.toSet()
    }

    companion object {
        private const val PATH = "config/case/%s/%s/bpmn/%s.bpmn"
    }
}