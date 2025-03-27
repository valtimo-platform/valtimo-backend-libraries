package com.ritense.processlink.uicomponent.service

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UIComponentSupportedProcessLinksHandlerTest {
    val handler = UIComponentSupportedProcessLinksHandler()

    @Test
    fun `should support USER_TASK_CREATE`() {

        val processLinkType = handler.getProcessLinkType(ActivityTypeWithEventName.USER_TASK_CREATE.value)
        assertThat(processLinkType).isNotNull
        assertThat(processLinkType!!.processLinkType).isEqualTo(UIComponentProcessLink.TYPE_UI_COMPONENT)
    }

    @Test
    fun `should support START_EVENT_START`() {

        val processLinkType = handler.getProcessLinkType(ActivityTypeWithEventName.START_EVENT_START.value)
        assertThat(processLinkType).isNotNull
        assertThat(processLinkType!!.processLinkType).isEqualTo(UIComponentProcessLink.TYPE_UI_COMPONENT)
    }
}