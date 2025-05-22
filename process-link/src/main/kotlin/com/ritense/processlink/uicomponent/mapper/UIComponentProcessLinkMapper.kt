/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processlink.uicomponent.mapper

import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkCreateRequestDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkDeployDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkExportResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkUpdateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import java.util.UUID

class UIComponentProcessLinkMapper : ProcessLinkMapper {

    override fun supportsProcessLinkType(processLinkType: String) = processLinkType == UIComponentProcessLink.TYPE_UI_COMPONENT

    override fun toProcessLinkResponseDto(processLink: ProcessLink): ProcessLinkResponseDto {
        processLink as UIComponentProcessLink
        return UIComponentProcessLinkResponseDto(
            id = processLink.id,
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            componentKey = processLink.componentKey
        )
    }

    override fun toProcessLinkCreateRequestDto(deployDto: ProcessLinkDeployDto): ProcessLinkCreateRequestDto {
        deployDto as UIComponentProcessLinkDeployDto
        return UIComponentProcessLinkCreateRequestDto(
            processDefinitionId = deployDto.processDefinitionId,
            activityId = deployDto.activityId,
            activityType = deployDto.activityType,
            componentKey = deployDto.componentKey
        )
    }

    override fun toProcessLinkExportResponseDto(processLink: ProcessLink): ProcessLinkExportResponseDto {
        processLink as UIComponentProcessLink
        return UIComponentProcessLinkExportResponseDto (
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            componentKey = processLink.componentKey
        )
    }

    override fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink {
        createRequestDto as UIComponentProcessLinkCreateRequestDto
        return UIComponentProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = createRequestDto.processDefinitionId,
            activityId = createRequestDto.activityId,
            activityType = createRequestDto.activityType,
            componentKey = createRequestDto.componentKey
        )
    }

    override fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink {
        updateRequestDto as UIComponentProcessLinkUpdateRequestDto
        assert(processLinkToUpdate.id == updateRequestDto.id)
        return UIComponentProcessLink(
            id = updateRequestDto.id,
            processDefinitionId = processLinkToUpdate.processDefinitionId,
            activityId = processLinkToUpdate.activityId,
            activityType = processLinkToUpdate.activityType,
            componentKey = updateRequestDto.componentKey
        )
    }

    override fun toProcessLinkUpdateRequestDto(
        deployDto: ProcessLinkDeployDto,
        existingProcessLinkId: UUID
    ): ProcessLinkUpdateRequestDto {
        deployDto as UIComponentProcessLinkDeployDto
        return UIComponentProcessLinkUpdateRequestDto(
            id = existingProcessLinkId,
            componentKey = deployDto.componentKey
        )
    }
}