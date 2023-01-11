/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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
import org.springframework.context.event.EventListener

class CopyPluginActionsOnProcessDeploymentListener(
    private val pluginProcessLinkRepository: PluginProcessLinkRepository,
) {

    @EventListener(ProcessDefinitionDeployedEvent::class)
    fun copyPluginLinks(event: ProcessDefinitionDeployedEvent) {
        val previousProcessDefinitionId = event.processDefinition.previousProcessDefinitionId

        if (previousProcessDefinitionId != null) {
            val newProcessDefinitionId = event.processDefinition.id
            val newActivities = event.processDefinition.activities

            val newLinks = pluginProcessLinkRepository.findByProcessDefinitionId(previousProcessDefinitionId)
                .filter { link -> newActivities.any { newActivity -> newActivity.id == link.activityId } }
                .map { link -> link.copy(id = PluginProcessLinkId.newId(), processDefinitionId = newProcessDefinitionId) }

            newLinks.forEach { newLink ->
                logger.debug { "Copying plugin action link to newly deployed process. Process: '${newLink.processDefinitionId}', activity: '${newLink.activityId}', plugin action: '${newLink.pluginActionDefinitionKey}'." }
            }

            pluginProcessLinkRepository.saveAll(newLinks)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
