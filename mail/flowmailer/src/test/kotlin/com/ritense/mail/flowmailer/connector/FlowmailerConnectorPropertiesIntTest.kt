package com.ritense.mail.flowmailer.connector

import com.ritense.mail.flowmailer.BaseIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

internal class FlowmailerConnectorPropertiesIntTest: BaseIntegrationTest() {

    @Inject
    lateinit var flowmailerConnectorProperties: FlowmailerConnectorProperties

    @Test
    fun `should instantiate FlowmailerConnector bean`() {
        Assertions.assertThat(flowmailerConnectorProperties).isNotNull
    }
}