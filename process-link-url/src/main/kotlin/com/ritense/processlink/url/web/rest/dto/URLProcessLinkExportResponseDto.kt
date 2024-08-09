package com.ritense.processlink.url.web.rest.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.url.mapper.URLProcessLinkMapper.Companion.PROCESS_LINK_TYPE_URL
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto

@JsonTypeName(PROCESS_LINK_TYPE_URL)
class URLProcessLinkExportResponseDto(
    override val activityId: String,
    override val activityType: ActivityTypeWithEventName,
    val url: String
) : ProcessLinkExportResponseDto {
    override val processLinkType: String
        get() = PROCESS_LINK_TYPE_URL
}