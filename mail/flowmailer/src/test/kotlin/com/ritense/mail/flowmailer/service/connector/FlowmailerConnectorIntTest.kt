package com.ritense.mail.flowmailer.service.connector

import com.ritense.mail.flowmailer.BaseIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

class FlowmailerConnectorIntTest: BaseIntegrationTest() {

    @Inject
    lateinit var flowmailerConnector: FlowmailerConnector

    @Test
    fun `should instantiate FlowmailerConnector bean`() {
        Assertions.assertThat(flowmailerConnector).isNotNull
    }
}