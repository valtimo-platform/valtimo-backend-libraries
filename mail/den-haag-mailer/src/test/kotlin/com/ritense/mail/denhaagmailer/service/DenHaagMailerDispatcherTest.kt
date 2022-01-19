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

package com.ritense.mail.denhaagmailer.service

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.connector.service.ConnectorService
import com.ritense.mail.denhaagmailer.BaseTest
import com.ritense.mail.denhaagmailer.connector.DenHaagMailerConnector
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class DenHaagMailerDispatcherTest : BaseTest() {
    lateinit var connectorService: ConnectorService
    lateinit var denHaagMailerMailDispatcher: DenHaagMailDispatcher
    lateinit var denHaagMailerConnector: DenHaagMailerConnector

    @BeforeEach
    internal fun setUp() {
        connectorService = mock(ConnectorService::class.java)
        denHaagMailerMailDispatcher = DenHaagMailDispatcher(connectorService)
        denHaagMailerConnector = mock(DenHaagMailerConnector::class.java);

        whenever(connectorService.loadByClassName(DenHaagMailerConnector::class.java))
            .thenReturn(denHaagMailerConnector)
    }

    @Test
    fun `send raw mail message should use connector`() {
        val rawMailMessage = mock(RawMailMessage::class.java)

        denHaagMailerMailDispatcher.send(rawMailMessage)

        verify(denHaagMailerConnector, times(1)).send(rawMailMessage)
    }

    @Test
    fun `send templated mail message should use connector`() {
        val templatedMailMessage = mock(TemplatedMailMessage::class.java)

        denHaagMailerMailDispatcher.send(templatedMailMessage)

        verify(denHaagMailerConnector, times(1)).send(templatedMailMessage)
    }

}