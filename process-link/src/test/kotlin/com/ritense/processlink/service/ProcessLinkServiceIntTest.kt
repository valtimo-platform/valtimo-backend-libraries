package com.ritense.processlink.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.AnotherTestProcessLink
import com.ritense.processlink.domain.AnotherTestProcessLinkUpdateRequestDto
import com.ritense.processlink.domain.ProcessLinkType
import com.ritense.processlink.domain.TestProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class ProcessLinkServiceIntTest @Autowired constructor(
    private val processLinkService: ProcessLinkService
): BaseIntegrationTest() {

    @Test
    fun `should update existing processLink to new type`(): Unit = runWithoutAuthorization {
        val processLink = processLinkService.getProcessLinksByProcessDefinitionKey("auto-deploy-process-link-with-long-key")
            .filterIsInstance<TestProcessLink>().first()

        processLinkService.updateProcessLink(
            AnotherTestProcessLinkUpdateRequestDto(
                processLink.id,
                "success!"
            )
        )

        val updatedProcessLink = processLinkService.getProcessLink(processLink.id, AnotherTestProcessLink::class.java)
        assertThat(updatedProcessLink.processLinkType).isEqualTo(AnotherTestProcessLink.PROCESS_LINK_TYPE)
        assertThat(updatedProcessLink.anotherValue).isNotEqualTo(processLink.someValue)
        assertThat(updatedProcessLink.anotherValue).isEqualTo("success!")
    }

    @Test
    fun `should get ordered list of supported process link types`() {
        val processLinkTypes = processLinkService.getSupportedProcessLinkTypes(ActivityTypeWithEventName.USER_TASK_CREATE.value)
        assertThat(processLinkTypes).containsExactly(
            ProcessLinkType(TestProcessLink.PROCESS_LINK_TYPE_TEST, true),
            ProcessLinkType(UIComponentProcessLink.TYPE_UI_COMPONENT, true),
            ProcessLinkType(AnotherTestProcessLink.PROCESS_LINK_TYPE, true),
        )
    }
}