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

package com.ritense.processlink.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exporter.request.ExportRequest
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.AnotherTestProcessLink.Companion.PROCESS_LINK_TYPE
import com.ritense.processlink.exporter.CustomProcessLinkNestedExportRequest
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import java.util.UUID

class AnotherTestProcessLinkMapper(
    objectMapper: ObjectMapper
) : ProcessLinkMapper {

    init {
        objectMapper.registerSubtypes(
            AnotherTestProcessLinkCreateRequestDto::class.java,
            AnotherTestProcessLinkDeployDto::class.java,
            AnotherTestProcessLinkExportResponseDto::class.java,
            AnotherTestProcessLinkResponseDto::class.java,
            AnotherTestProcessLinkUpdateRequestDto::class.java
        )
    }

    override fun supportsProcessLinkType(processLinkType: String) = processLinkType == PROCESS_LINK_TYPE

    override fun toProcessLinkResponseDto(processLink: ProcessLink): ProcessLinkResponseDto {
        processLink as AnotherTestProcessLink
        return AnotherTestProcessLinkResponseDto(
            id = processLink.id,
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            anotherValue = processLink.anotherValue
        )
    }

    override fun toProcessLinkUpdateRequestDto(
        deployDto: ProcessLinkDeployDto,
        existingProcessLinkId: UUID
    ): ProcessLinkUpdateRequestDto {
        deployDto as AnotherTestProcessLinkDeployDto
        return AnotherTestProcessLinkUpdateRequestDto(
            id = existingProcessLinkId,
            anotherValue = deployDto.anotherValue
        )
    }

    override fun toProcessLinkCreateRequestDto(deployDto: ProcessLinkDeployDto): ProcessLinkCreateRequestDto {
        deployDto as AnotherTestProcessLinkDeployDto
        return AnotherTestProcessLinkCreateRequestDto(
            processDefinitionId = deployDto.processDefinitionId,
            activityId = deployDto.activityId,
            activityType = deployDto.activityType,
            anotherValue = deployDto.anotherValue
        )
    }

    override fun toProcessLinkExportResponseDto(processLink: ProcessLink): ProcessLinkExportResponseDto {
        processLink as AnotherTestProcessLink
        return AnotherTestProcessLinkExportResponseDto(
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            anotherValue = processLink.anotherValue
        )
    }

    override fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink {
        createRequestDto as AnotherTestProcessLinkCreateRequestDto
        return AnotherTestProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = createRequestDto.processDefinitionId,
            activityId = createRequestDto.activityId,
            activityType = createRequestDto.activityType,
            anotherValue = createRequestDto.anotherValue
        )
    }

    override fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink {
        updateRequestDto as AnotherTestProcessLinkUpdateRequestDto

        return AnotherTestProcessLink(
            id = updateRequestDto.id,
            processDefinitionId = processLinkToUpdate.processDefinitionId,
            activityId = processLinkToUpdate.activityId,
            activityType = processLinkToUpdate.activityType,
            anotherValue = updateRequestDto.anotherValue
        )
    }

    override fun createRelatedExportRequests(processLink: ProcessLink): Set<ExportRequest> {
        return setOf(CustomProcessLinkNestedExportRequest())
    }

    override fun getImporterType() = PROCESS_LINK_TYPE
}
