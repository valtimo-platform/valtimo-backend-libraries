package com.ritense.mail.flowmailer.service

import com.ritense.mail.flowmailer.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.inject.Inject

internal class FlowmailerTokenServiceIntTest: BaseIntegrationTest() {

    @Inject
    lateinit var flowmailerTokenService: FlowmailerTokenService

    @Test
    fun `should instantiate FlowmailerTokenService bean`() {
        assertThat(flowmailerTokenService).isNotNull
    }
}