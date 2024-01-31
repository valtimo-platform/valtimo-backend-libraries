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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.processlink.mapper.PluginProcessLinkMapper
import com.ritense.valtimo.processlink.service.PluginSupportedProcessLinksHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class ProcessLinkAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ProcessLinkServiceTaskStartListener::class)
    fun pluginLinkServiceTaskStartListener(
        pluginProcessLinkRepository: PluginProcessLinkRepository?,
        pluginService: PluginService?
    ): ProcessLinkServiceTaskStartListener {
        return ProcessLinkServiceTaskStartListener(
            pluginProcessLinkRepository!!,
            pluginService!!
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkUserTaskCreateListener::class)
    fun processLinkUserTaskCreateListener(
        pluginProcessLinkRepository: PluginProcessLinkRepository?,
        pluginService: PluginService?
    ): ProcessLinkUserTaskCreateListener {
        return ProcessLinkUserTaskCreateListener(
            pluginProcessLinkRepository!!,
            pluginService!!
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkCallActivityStartListener::class)
    fun processLinkCallActivityStartListener(
        pluginProcessLinkRepository: PluginProcessLinkRepository?,
        pluginService: PluginService?
    ): ProcessLinkCallActivityStartListener {
        return ProcessLinkCallActivityStartListener(
            pluginProcessLinkRepository!!,
            pluginService!!
        )
    }

    @Bean
    @ConditionalOnMissingBean(PluginProcessLinkMapper::class)
    fun pluginProcessLinkMapper(
        objectMapper: ObjectMapper
    ): PluginProcessLinkMapper {
        return PluginProcessLinkMapper(objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean(PluginSupportedProcessLinksHandler::class)
    fun pluginSupportedProcessLinksHandler(pluginService: PluginService): PluginSupportedProcessLinksHandler {
        return PluginSupportedProcessLinksHandler(pluginService)
    }
}
