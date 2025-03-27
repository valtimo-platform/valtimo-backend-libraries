package com.ritense.processlink.uicomponent.service

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.TestProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink.Companion.TYPE_UI_COMPONENT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.UUID

class UIComponentProcessLinkActivityHandlerTest {
    val handler = UIComponentProcessLinkActivityHandler()

    @Test
    fun `should support type`() {
        val supports = handler.supports(createProcessLink())
        assertThat(supports).isTrue
    }

    @Test
    fun `should not support other type`() {
        val supports = handler.supports(createTestProcessLink())
        assertThat(supports).isFalse()
    }

    @Test
    fun `should return result for openTask`() {
        val processLink = createProcessLink()
        val result = handler.openTask(
            task = mock(),
            processLink = processLink
        )
        assertThat(result.processLinkId).isEqualTo(processLink.id)
        assertThat(result.type).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(result.properties.componentKey).isEqualTo("componentKey")
    }

    @Test
    fun `should return result for getStartEventObject`() {
        val processLink = createProcessLink()
        val result = handler.getStartEventObject(
            processDefinitionId = "processDefinitionId",
            documentId = UUID.randomUUID(),
            documentDefinitionName = "documentDefinitionName",
            processLink = processLink
        )
        assertThat(result.processLinkId).isEqualTo(processLink.id)
        assertThat(result.type).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(result.properties.componentKey).isEqualTo("componentKey")
    }

    private fun createProcessLink() = UIComponentProcessLink(
        id = UUID.randomUUID(),
        processDefinitionId = "processDefinitionId",
        activityId = "activityId",
        activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
        componentKey = "componentKey",
    )

    private fun createTestProcessLink() = TestProcessLink(
        id = UUID.randomUUID(),
        processDefinitionId = "processDefinitionId",
        activityId = "activityId",
        activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
        someValue = "someValue",
    )
}