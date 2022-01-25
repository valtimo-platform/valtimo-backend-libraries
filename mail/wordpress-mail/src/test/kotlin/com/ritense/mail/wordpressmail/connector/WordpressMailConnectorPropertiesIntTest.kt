package com.ritense.mail.wordpressmail.connector

import com.ritense.mail.wordpressmail.BaseIntegrationTest
import javax.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WordpressMailConnectorPropertiesIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var wordpressMailConnectorProperties: WordpressMailConnectorProperties

    @Test
    fun `should instantiate WordpressMailConnector bean`() {
        assertThat(wordpressMailConnectorProperties).isNotNull
    }
}