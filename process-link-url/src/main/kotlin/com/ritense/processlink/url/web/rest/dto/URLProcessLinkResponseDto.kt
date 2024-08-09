package com.ritense.processlink.url.web.rest.dto

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.url.mapper.URLProcessLinkMapper.Companion.PROCESS_LINK_TYPE_URL
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import java.util.UUID

data class URLProcessLinkResponseDto(
    override val id: UUID,
    override val processDefinitionId: String,
    override val activityId: String,
    override val activityType: ActivityTypeWithEventName,
    override val processLinkType: String = PROCESS_LINK_TYPE_URL,
    val url: String
) : ProcessLinkResponseDto