package com.ritense.processlink.url.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.url.domain.URLProcessLink
import com.ritense.processlink.url.web.rest.dto.URLProcessLinkDeployDto
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.processlink.url.web.rest.dto.URLProcessLinkCreateRequestDto
import com.ritense.processlink.url.web.rest.dto.URLProcessLinkExportResponseDto
import com.ritense.processlink.url.web.rest.dto.URLProcessLinkResponseDto
import com.ritense.processlink.url.web.rest.dto.URLProcessLinkUpdateRequestDto
import java.util.UUID

class URLProcessLinkMapper(
    objectMapper: ObjectMapper
) : ProcessLinkMapper {

    init {
        objectMapper.registerSubtypes(
            URLProcessLinkCreateRequestDto::class.java,
            URLProcessLinkResponseDto::class.java,
            URLProcessLinkDeployDto::class.java,
            URLProcessLinkExportResponseDto::class.java,
            URLProcessLinkUpdateRequestDto::class.java
        )
    }

    override fun supportsProcessLinkType(processLinkType: String) = processLinkType == PROCESS_LINK_TYPE_URL

    override fun toProcessLinkResponseDto(processLink: ProcessLink): ProcessLinkResponseDto {
        processLink as URLProcessLink
        return URLProcessLinkResponseDto(
            id = processLink.id,
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            url = processLink.url
        )
    }

    override fun toProcessLinkCreateRequestDto(deployDto: ProcessLinkDeployDto): ProcessLinkCreateRequestDto {
        deployDto as URLProcessLinkDeployDto
        return URLProcessLinkCreateRequestDto(
            processDefinitionId = deployDto.processDefinitionId,
            activityId = deployDto.activityId,
            activityType = deployDto.activityType,
            url = deployDto.url
        )
    }

    override fun toProcessLinkExportResponseDto(processLink: ProcessLink): ProcessLinkExportResponseDto {
        processLink as URLProcessLink
        return URLProcessLinkExportResponseDto (
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            url = processLink.url
        )
    }

    override fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink {
        createRequestDto as URLProcessLinkCreateRequestDto
        return URLProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = createRequestDto.processDefinitionId,
            activityId = createRequestDto.activityId,
            activityType = createRequestDto.activityType,
            url = createRequestDto.url
        )
    }

    override fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink {
        updateRequestDto as URLProcessLinkUpdateRequestDto
        assert(processLinkToUpdate.id == updateRequestDto.id)
        return URLProcessLink(
            id = updateRequestDto.id,
            processDefinitionId = processLinkToUpdate.processDefinitionId,
            activityId = processLinkToUpdate.activityId,
            activityType = processLinkToUpdate.activityType,
            url = updateRequestDto.url
        )
    }

    companion object {
        const val PROCESS_LINK_TYPE_URL = "url"
    }
}