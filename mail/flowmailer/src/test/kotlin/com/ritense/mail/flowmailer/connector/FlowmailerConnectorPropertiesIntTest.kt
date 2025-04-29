package com.ritense.mail.flowmailer.connector

import com.ritense.mail.flowmailer.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class FlowmailerConnectorPropertiesIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var flowmailerConnectorProperties: FlowmailerConnectorProperties

    @Test
    fun `should instantiate FlowmailerConnector bean`() {
        assertThat(flowmailerConnectorProperties).isNotNull
    }
}