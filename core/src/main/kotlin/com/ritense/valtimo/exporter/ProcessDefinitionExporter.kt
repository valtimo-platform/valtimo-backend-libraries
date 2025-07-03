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
import com.ritense.valtimo.operaton.repository.OperatonDecisionDefinitionSpecificationHelper
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byVersion
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byVersionTag
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.operaton.bpm.engine.RepositoryService
import org.operaton.bpm.model.bpmn.Bpmn
import org.operaton.bpm.model.bpmn.BpmnModelInstance
import org.operaton.bpm.model.bpmn.instance.BusinessRuleTask
import org.operaton.bpm.model.bpmn.instance.CallActivity
import java.io.ByteArrayOutputStream

class ProcessDefinitionExporter(
    private val operatonRepositoryService: OperatonRepositoryService,
    private val repositoryService: RepositoryService,
) : Exporter<ProcessDefinitionExportRequest> {
    override fun supports(): Class<ProcessDefinitionExportRequest> = ProcessDefinitionExportRequest::class.java

    override fun export(request: ProcessDefinitionExportRequest): ExportResult {
        val processDefinition = requireNotNull(
            operatonRepositoryService.findProcessDefinitionById(request.processDefinitionId)
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
                    when (it.operatonCalledElementBinding) {
                        "version" -> operatonRepositoryService.findProcessDefinition(spec.and(byVersion(it.operatonCalledElementVersion.toInt())))
                        "versionTag" -> operatonRepositoryService.findProcessDefinition(spec.and(byVersionTag(it.operatonCalledElementVersionTag)))
                        "deployment" -> null
                        else -> operatonRepositoryService.findProcessDefinition(spec.and(byLatestVersion()))
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
            if (it.operatonDecisionRef != null) {
                val spec = OperatonDecisionDefinitionSpecificationHelper.byKey(it.operatonDecisionRef)
                val decisionDefinitionId = checkNotNull(
                    when (it.operatonDecisionRefBinding) {
                        "version" -> operatonRepositoryService.findDecisionDefinition(spec.and(OperatonDecisionDefinitionSpecificationHelper.byVersion(it.operatonDecisionRefVersion.toInt())))
                        "versionTag" -> operatonRepositoryService.findDecisionDefinition(spec.and(OperatonDecisionDefinitionSpecificationHelper.byVersionTag(it.operatonDecisionRefVersionTag)))
                        "deployment" -> null
                        else -> operatonRepositoryService.findDecisionDefinition(spec.and(OperatonDecisionDefinitionSpecificationHelper.byLatestVersion()))
                    }
                ) {
                    "Decision definition with reference '${it.operatonDecisionRef}' could not be found!"
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