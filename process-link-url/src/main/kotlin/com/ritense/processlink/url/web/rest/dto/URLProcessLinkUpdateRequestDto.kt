package com.ritense.processlink.url.web.rest.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.url.mapper.URLProcessLinkMapper.Companion.PROCESS_LINK_TYPE_URL
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import java.util.UUID

@JsonTypeName(PROCESS_LINK_TYPE_URL)

data class URLProcessLinkUpdateRequestDto(
    override val id: UUID,
    val url: String
) : ProcessLinkUpdateRequestDto {
    override val processLinkType: String
        get() = PROCESS_LINK_TYPE_URL
}