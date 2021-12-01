package com.ritense.mail.flowmailer.config

import com.ritense.mail.flowmailer.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.inject.Inject

internal class FlowmailerPropertiesTest: BaseIntegrationTest() {

    @Inject
    lateinit var flowmailerProperties: FlowmailerProperties

    @Test
    fun `should create a FlowmailerProperties bean`() {
        assertThat(flowmailerProperties).isNotNull
        assertThat(flowmailerProperties.accountId).isEqualTo("accountId")
    }
}
