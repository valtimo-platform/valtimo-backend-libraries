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

package com.ritense.processdocument.listener

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.domain.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import com.ritense.valtimo.service.CamundaProcessService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class ProcessDefinitionCaseEventListener(
    private val processService: CamundaProcessService,
    private val associationService: ProcessDefinitionCaseDefinitionService,
) {

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (event.duplicate) {
            val associations = associationService.findProcessDefinitionCaseDefinitions(event.basedOnCaseDefinitionId!!)

            processService.getDeployedDefinitions(event.basedOnCaseDefinitionId!!).forEach { oldProcessDefinition ->
                val association = associations.firstOrNull { it.processDefinitionKey == oldProcessDefinition.key }
                if (association == null) {
                    logger.error { "Missing association between caseDefinitionId ${event.basedOnCaseDefinitionId} and processDefinitionKey ${oldProcessDefinition.key}" }
                } else {
                    val deploymentId = processService.deploy(
                        event.caseDefinitionId,
                        oldProcessDefinition.resourceName,
                        processService.getBpmnModel(oldProcessDefinition).inputStream()
                    )
                        ?.id
                        ?: error { "Failed to duplicate process definition ${oldProcessDefinition.key} for ${event.caseDefinitionId}" }

                    val processDefinition = processService.getProcessDefinitionByDeploymentId(deploymentId)
                    associationService.createProcessDocumentDefinition(
                        ProcessDocumentDefinitionRequest(
                            processDefinitionId = ProcessDefinitionId(processDefinition.id),
                            caseDefinitionId = event.caseDefinitionId,
                            canInitializeDocument = association.canInitializeDocument,
                            startableByUser = association.startableByUser,
                        )
                    )
                }
            }
        }
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionPreDeleteEvent::class)
    fun handleCaseDefinitionPreDeleteEvent(event: CaseDefinitionPreDeleteEvent) {
        associationService.findProcessDefinitionCaseDefinitions(event.caseDefinitionId).forEach { association ->
            associationService.deleteProcessDocumentDefinition(
                processDefinitionId = association.id.processDefinitionId,
                caseDefinitionId = event.caseDefinitionId,
            )
        }
        processService.getDeployedDefinitions(event.caseDefinitionId).forEach { definition ->
            processService.deleteProcessDefinition(definition.id)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}