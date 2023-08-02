/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.contactmoment.service

import com.ritense.contactmoment.domain.request.SendMessageRequest
import com.ritense.klant.domain.Klant
import com.ritense.klant.service.KlantService
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

internal class KlantcontactServiceTest {

    @Test
    fun `sendMessage should get klant and send mail`() {
        val mailSender = mock<MailSender>()
        val klantService = mock<KlantService>()
        val template = "template"
        val service = KlantcontactService(mailSender, klantService, template)

        val captor = argumentCaptor<TemplatedMailMessage>()
        val documentId = UUID.randomUUID()
        val request = SendMessageRequest("subject", "text")

        whenever(klantService.getKlantForDocument(documentId)).thenReturn(
            Klant(
                "http://example.org",
                "0612345678",
                "user@example.org"
            )
        )

        service.sendMessage(documentId, request)

        verify(klantService).getKlantForDocument(documentId)
        verify(mailSender).send(captor.capture())

        val sentMessage = captor.firstValue
        assertTrue(sentMessage.recipients.isPresent)
        assertThat(sentMessage.recipients.get().size, equalTo(1))

        val recipient = sentMessage.recipients.get().first()
        assertThat(recipient.email.get(), equalTo("user@example.org"))
        assertThat(recipient.name.get(), equalTo("user@example.org"))
        assertThat(recipient.type, equalTo(Recipient.Type.TO))

        assertThat(sentMessage.templateIdentifier.get(), equalTo("template"))
        assertThat(sentMessage.subject.get(), equalTo("subject"))
        assertThat(sentMessage.placeholders, hasEntry("bodyText", "text"))
        assertThat(sentMessage.placeholders, hasEntry("subject", "subject"))
    }

    @Test
    fun `sendMessage should throw exception when emailadress is null`() {
        val mailSender = mock<MailSender>()
        val klantService = mock<KlantService>()
        val template = "template"
        val service = KlantcontactService(mailSender, klantService, template)

        val documentId = UUID.randomUUID()
        val request = SendMessageRequest("subject", "text")

        whenever(klantService.getKlantForDocument(documentId)).thenReturn(
            Klant(
                "http://example.org",
                "0612345678",
                null
            )
        )

        val exception = Assertions.assertThrows(IllegalStateException::class.java) {
            service.sendMessage(documentId, request)
        }

        Assertions.assertEquals("emailaddress was not available for klant", exception.message)
    }

}

