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

package com.ritense.valtimo.processlink.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.service.PluginService.Companion.PROCESS_LINK_TYPE_PLUGIN
import com.ritense.plugin.web.rest.request.PluginProcessLinkCreateDto
import com.ritense.plugin.web.rest.request.PluginProcessLinkUpdateDto
import com.ritense.plugin.web.rest.result.PluginProcessLinkResultDto
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto

class PluginProcessLinkMapper(
    objectMapper: ObjectMapper
) : ProcessLinkMapper {

    init {
        objectMapper.registerSubtypes(
            PluginProcessLinkResultDto::class.java,
            PluginProcessLinkCreateDto::class.java,
            PluginProcessLinkUpdateDto::class.java,
            PluginProcessLinkDeployDto::class.java,
        )
    }

    override fun supportsProcessLinkType(processLinkType: String) = processLinkType == PROCESS_LINK_TYPE_PLUGIN

    override fun toProcessLinkResponseDto(processLink: ProcessLink): ProcessLinkResponseDto {
        processLink as PluginProcessLink
        return PluginProcessLinkResultDto(
            id = processLink.id,
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            pluginConfigurationId = processLink.pluginConfigurationId.id,
            pluginActionDefinitionKey = processLink.pluginActionDefinitionKey,
            actionProperties = processLink.actionProperties,
        )
    }

    override fun toProcessLinkCreateRequestDto(deployDto: ProcessLinkDeployDto): ProcessLinkCreateRequestDto {
        deployDto as PluginProcessLinkDeployDto
        return PluginProcessLinkCreateDto(
            processDefinitionId = deployDto.processDefinitionId,
            activityId = deployDto.activityId,
            pluginConfigurationId = deployDto.pluginConfigurationId,
            pluginActionDefinitionKey = deployDto.pluginActionDefinitionKey,
            actionProperties = deployDto.actionProperties,
            activityType = deployDto.activityType,
        )
    }

    override fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink {
        createRequestDto as PluginProcessLinkCreateDto
        return PluginProcessLink(
            id = createRequestDto.processLinkId,
            processDefinitionId = createRequestDto.processDefinitionId,
            activityId = createRequestDto.activityId,
            activityType = createRequestDto.activityType,
            pluginConfigurationId = PluginConfigurationId.existingId(createRequestDto.pluginConfigurationId),
            pluginActionDefinitionKey = createRequestDto.pluginActionDefinitionKey,
            actionProperties = createRequestDto.actionProperties,
        )
    }

    override fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink {
        updateRequestDto as PluginProcessLinkUpdateDto
        return PluginProcessLink(
            id = updateRequestDto.id,
            processDefinitionId = processLinkToUpdate.processDefinitionId,
            activityId = processLinkToUpdate.activityId,
            activityType = processLinkToUpdate.activityType,
            pluginConfigurationId = PluginConfigurationId.existingId(updateRequestDto.pluginConfigurationId),
            pluginActionDefinitionKey = updateRequestDto.pluginActionDefinitionKey,
            actionProperties = updateRequestDto.actionProperties,
        )
    }

}