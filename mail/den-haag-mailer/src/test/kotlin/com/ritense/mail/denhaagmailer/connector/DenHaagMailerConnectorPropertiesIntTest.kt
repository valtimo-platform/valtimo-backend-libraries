package com.ritense.mail.denhaagmailer.connector

import com.ritense.mail.denhaagmailer.BaseIntegrationTest
import javax.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DenHaagMailerConnectorPropertiesIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var denHaagMailerConnectorProperties: DenHaagMailerConnectorProperties

    @Test
    fun `should instantiate DenHaagMailerConnector bean`() {
        assertThat(denHaagMailerConnectorProperties).isNotNull
    }
}