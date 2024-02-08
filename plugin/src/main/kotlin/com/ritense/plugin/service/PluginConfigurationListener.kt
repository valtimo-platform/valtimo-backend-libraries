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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.events.PluginConfigurationIdUpdatedEvent
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import org.springframework.context.event.EventListener

class PluginConfigurationListener(
    private val pluginConfigurationRepository: PluginConfigurationRepository,
    private val pluginProcessLinkRepository: PluginProcessLinkRepository,
) {

    @EventListener(PluginConfigurationIdUpdatedEvent::class)
    fun handle(event: PluginConfigurationIdUpdatedEvent) {
        val processLinks = pluginProcessLinkRepository.findByPluginConfigurationId(PluginConfigurationId.existingId(event.oldId))
            .map { it.copy(pluginConfigurationId = PluginConfigurationId.existingId(event.newId)) }
        pluginProcessLinkRepository.saveAll(processLinks)
        val configurations = pluginConfigurationRepository.findAll()
        configurations.forEach { configuration ->
            configuration.rawProperties?.fields()?.forEachRemaining { property ->
                if (property.value.textValue() == event.oldId.toString()) {
                    property.setValue(TextNode.valueOf(event.newId.toString()))
                }
            }
        }
        pluginConfigurationRepository.saveAll(configurations)
    }
}