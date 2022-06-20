/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.plugin

import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener

class PluginDeploymentListener(
    private val pluginDefinitionResolver: PluginDefinitionResolver,
    private val pluginDefinitionRepository: PluginDefinitionRepository
) {

    @EventListener(ApplicationStartedEvent::class)
    fun deployPluginDefinitions() {
        val classes = findPluginClasses()

        classes.forEach { (clazz, pluginAnnotation) ->
            try {
                deployPluginDefinition(
                    PluginDefinition(pluginAnnotation.key, pluginAnnotation.title, pluginAnnotation.description, clazz.name)
                )
            } catch (e: Exception) {
                throw PluginDefinitionNotDeployedException(pluginAnnotation.key, clazz.name, e)
            }
        }
    }

    private fun findPluginClasses() : Map<Class<*>, Plugin> {
        return pluginDefinitionResolver.findPluginClasses()
    }

    private fun deployPluginDefinition(pluginDefinition: PluginDefinition): PluginDefinition {
        return pluginDefinitionRepository.save(pluginDefinition)
    }
}