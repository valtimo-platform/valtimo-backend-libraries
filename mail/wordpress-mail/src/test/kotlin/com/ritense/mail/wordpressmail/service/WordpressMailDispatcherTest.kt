/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.mail.wordpressmail.service

import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import com.ritense.connector.service.ConnectorService
import com.ritense.mail.wordpressmail.BaseTest
import com.ritense.mail.wordpressmail.connector.WordpressMailConnector
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class WordpressMailDispatcherTest : BaseTest() {
    lateinit var connectorService: ConnectorService
    lateinit var wordpressMailMailDispatcher: WordpressMailDispatcher
    lateinit var wordpressMailConnector: WordpressMailConnector

    @BeforeEach
    internal fun setUp() {
        connectorService = mock(ConnectorService::class.java)
        wordpressMailMailDispatcher = WordpressMailDispatcher(connectorService)
        wordpressMailConnector = mock(WordpressMailConnector::class.java);

        whenever(connectorService.loadByClassName(WordpressMailConnector::class.java))
            .thenReturn(wordpressMailConnector)
    }

    @Test
    fun `send raw mail message should use connector`() {
        val rawMailMessage = mock(RawMailMessage::class.java)

        wordpressMailMailDispatcher.send(rawMailMessage)

        verify(wordpressMailConnector, times(1)).send(rawMailMessage)
    }

    @Test
    fun `send templated mail message should use connector`() {
        val templatedMailMessage = mock(TemplatedMailMessage::class.java)

        wordpressMailMailDispatcher.send(templatedMailMessage)

        verify(wordpressMailConnector, times(1)).send(templatedMailMessage)
    }

}