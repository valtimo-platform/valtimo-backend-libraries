package com.ritense.mail.wordpressmail.connector

import com.ritense.mail.wordpressmail.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class WordpressMailConnectorPropertiesIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var wordpressMailConnectorProperties: WordpressMailConnectorProperties

    @Test
    fun `should instantiate WordpressMailConnector bean`() {
        assertThat(wordpressMailConnectorProperties).isNotNull
    }
}