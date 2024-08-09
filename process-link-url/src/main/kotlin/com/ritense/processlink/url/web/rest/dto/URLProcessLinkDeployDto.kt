package com.ritense.processlink.url.web.rest.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.url.mapper.URLProcessLinkMapper.Companion.PROCESS_LINK_TYPE_URL

@JsonTypeName(PROCESS_LINK_TYPE_URL)
class URLProcessLinkDeployDto(
    override val processDefinitionId: String,
    override val activityId: String,
    override val activityType: ActivityTypeWithEventName,
    val url: String
) : ProcessLinkDeployDto {
    override val processLinkType: String
        get() = PROCESS_LINK_TYPE_URL
}