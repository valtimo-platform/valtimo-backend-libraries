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

package com.ritense.processlink.service

import com.ritense.processlink.domain.ProcessLinksCopiedEvent
import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.valtimo.camunda.domain.CamundaDeploymentSource
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.event.ProcessDefinitionDeployedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import java.util.UUID

class CopyProcessLinkOnProcessDeploymentListener(
    private val processLinkRepository: ProcessLinkRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @EventListener(ProcessDefinitionDeployedEvent::class)
    fun copyProcessLinks(event: ProcessDefinitionDeployedEvent) {
        if (event.source.skipProcessLinksCopy == true) {
            return
        }

        val originalProcessDefinitionId = event.source.originalProcessDefinitionId ?: event.previousProcessDefinitionId

        if (originalProcessDefinitionId != null) {
            val modelInstance = event.processDefinitionModelInstance

            val newLinks = processLinkRepository.findByProcessDefinitionId(originalProcessDefinitionId)
                .filter { link -> modelInstance.getModelElementById<FlowNode>(link.activityId) != null }
                .filter { link ->
                    processLinkRepository.findByProcessDefinitionIdAndActivityId(
                        event.processDefinitionId,
                        link.activityId
                    ).isEmpty()
                }
                .onEach { link ->
                    logger.debug { "Copying process link from original process with id ${originalProcessDefinitionId} to newly deployed process with id ${event.processDefinitionId}. Activity: '${link.activityId}', type: '${link.processLinkType}'." }
                }.map { link ->
                    link.copy(id = UUID.randomUUID(), processDefinitionId = event.processDefinitionId)
                }

            processLinkRepository.saveAll(newLinks)

            applicationEventPublisher.publishEvent(
                ProcessLinksCopiedEvent(
                    newLinks,
                    event.processDefinitionId,
                    event.caseDefinitionId,
                    event.source.originalProcessDefinitionId,
                    CaseDefinitionId.fromProcessVersionTag(event.source.originalVersionTag),
                )
            )
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
