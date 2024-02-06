package com.ritense.mail.flowmailer.connector

import com.ritense.mail.flowmailer.BaseIntegrationTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FlowmailerConnectorPropertiesIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var flowmailerConnectorProperties: FlowmailerConnectorProperties

    @Test
    fun `should instantiate FlowmailerConnector bean`() {
        assertThat(flowmailerConnectorProperties).isNotNull
    }
}