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

package com.ritense.mail.denhaagmailer.connector

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.service.DocumentService
import com.ritense.mail.denhaagmailer.BaseTest
import com.ritense.mail.denhaagmailer.domain.EmailSendRequest
import com.ritense.mail.denhaagmailer.domain.EmailSendResponse
import com.ritense.mail.denhaagmailer.domain.EmailTemplateResponse
import com.ritense.mail.denhaagmailer.service.DenHaagMailClient
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.apache.commons.lang3.NotImplementedException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class DenHaagMailerConnectorTest : BaseTest() {
    lateinit var denHaagMailerConnectorProperties: DenHaagMailerConnectorProperties
    lateinit var denHaagMailClient: DenHaagMailClient
    lateinit var denHaagMailerConnector: DenHaagMailerConnector
    lateinit var documentService: DocumentService

    @BeforeEach
    fun setup() {
        super.baseSetUp()
        denHaagMailerConnectorProperties = DenHaagMailerConnectorProperties("http://localhost:8012/")
        denHaagMailClient = mock(DenHaagMailClient::class.java)
        documentService = mock(DocumentService::class.java)
        denHaagMailerConnector = DenHaagMailerConnector(
            denHaagMailerConnectorProperties = denHaagMailerConnectorProperties,
            denHaagMailClient = denHaagMailClient
        )
    }

    @Test
    fun `should get properties`() {
        //when
        val properties = denHaagMailerConnector.getProperties()
        //then
        assertThat(properties).isNotNull
    }

    @Test
    fun `should set properties`() {
        //Given
        val denHaagMailerConnectorProperties = DenHaagMailerConnectorProperties("http://localhost:8012/")

        //when
        denHaagMailerConnector.setProperties(denHaagMailerConnectorProperties)
        val properties = denHaagMailerConnector.getProperties()

        //then
        assertThat(properties).isNotNull
        assertThat(properties).isInstanceOf(DenHaagMailerConnectorProperties::class.java)
        assertThat((properties as DenHaagMailerConnectorProperties).url).isEqualTo("http://localhost:8012/")
    }

    @Test
    fun `should throw NotImplementedException`() {
        val rawMailMessage = rawMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )
        val exception = Assertions.assertThrows(NotImplementedException::class.java) {
            denHaagMailerConnector.send(rawMailMessage)
        }
        assertThat(exception).hasMessageContaining("Send has not been implemented with RawMailMessage")
    }

    @Test
    fun `should build TemplatedMailMessage and call the send method in the DenHaagMailClient`() {
        //Given
        val templateResponse = EmailTemplateResponse.EmailTemplate("generic-mail-template", "generic-mail-template", "", "", emptyList())
        whenever(denHaagMailClient.getEmailTemplates())
            .thenReturn(EmailTemplateResponse(true, "", listOf(templateResponse)))
        whenever(denHaagMailClient.send(anyString(), any(), isNull()))
            .thenReturn(EmailSendResponse(true, "", EmailSendResponse.Data("John Doe <john@example.com>", "", "")))
        val templatedMailMessage =
            templatedMailMessage("john@example.com", "John Doe", "generic-mail-template", "Welcome", "VOORNAAM", "John")

        //When
        denHaagMailerConnector.send(templatedMailMessage)

        //Then
        val captor: ArgumentCaptor<EmailSendRequest> = ArgumentCaptor.forClass(EmailSendRequest::class.java)
        verify(denHaagMailClient).send(eq("generic-mail-template"), capture(captor), isNull())
        assertThat(captor.value.variables).containsEntry("VOORNAAM", "John")
        assertThat(captor.value.variables).containsEntry("SUBJECT", "Welcome")
        assertThat(captor.value.to).isEqualTo("John Doe <john@example.com>")
    }

}