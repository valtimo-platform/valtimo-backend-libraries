package com.ritense.processlink.url.service

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLinkType
import com.ritense.processlink.domain.SupportedProcessLinkTypeHandler

class URLSupportedProcessLinksHandler: SupportedProcessLinkTypeHandler {

    private val supportedActivityTypes = listOf(
        ActivityTypeWithEventName.USER_TASK_CREATE,
        ActivityTypeWithEventName.START_EVENT_START
    )

    override fun getProcessLinkType(activityType: String): ProcessLinkType? {
        if (supportedActivityTypes.contains(ActivityTypeWithEventName.fromValue(activityType))) {
            return ProcessLinkType("url", true)
        }
        return null
    }

}