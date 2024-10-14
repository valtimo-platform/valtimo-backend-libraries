/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.valtimo.processlink.service

import com.ritense.logging.LoggableResource
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.processlink.mapper.PluginProcessLinkMapper
import org.springframework.stereotype.Service

@Service
@SkipComponentScan
class PluginProcessLinkServiceImpl(
    private val processLinkService: ProcessLinkService,
    private val pluginProcessLinkMapper: PluginProcessLinkMapper,
) : PluginProcessLinkService {

    override fun getProcessLinks(
        @LoggableResource(resourceType = CamundaProcessDefinition::class) processDefinitionId: String
    ): List<PluginProcessLink> {
        return processLinkService.getProcessLinks(processDefinitionId)
            .filter { pluginProcessLinkMapper.supportsProcessLinkType(it.processLinkType) }
            .map { it as PluginProcessLink }
    }
}
