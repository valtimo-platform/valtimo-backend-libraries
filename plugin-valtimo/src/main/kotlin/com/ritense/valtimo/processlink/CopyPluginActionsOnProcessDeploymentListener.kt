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

package com.ritense.valtimo.processlink

import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.valtimo.event.ProcessDefinitionDeployedEvent
import mu.KotlinLogging
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.springframework.context.event.EventListener

class CopyPluginActionsOnProcessDeploymentListener(
    private val pluginProcessLinkRepository: PluginProcessLinkRepository
) {

    @EventListener(ProcessDefinitionDeployedEvent::class)
    fun copyPluginLinks(event: ProcessDefinitionDeployedEvent) {
        val previousProcessDefinitionId = event.previousProcessDefinitionId

        if (previousProcessDefinitionId != null) {
            val modelInstance = event.processDefinitionModelInstance

            val newLinks = pluginProcessLinkRepository.findByProcessDefinitionId(previousProcessDefinitionId)
                .filter { link -> modelInstance.getModelElementById<FlowNode>(link.activityId) != null }
                .filter { link ->
                    pluginProcessLinkRepository.findByProcessDefinitionIdAndActivityId(
                        event.processDefinitionId,
                        link.activityId
                    ).isEmpty()
                }
                .onEach { link ->
                    logger.debug { "Copying plugin action link to newly deployed process with id ${event.processDefinitionId}. Activity: '${link.activityId}', plugin action: '${link.pluginActionDefinitionKey}'." }
                }.map { link ->
                    link.copy(id = PluginProcessLinkId.newId(), processDefinitionId = event.processDefinitionId)
                }

            pluginProcessLinkRepository.saveAll(newLinks)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
