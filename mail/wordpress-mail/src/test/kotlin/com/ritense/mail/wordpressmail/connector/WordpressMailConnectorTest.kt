/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.mail.wordpressmail.connector

import com.ritense.document.service.DocumentService
import com.ritense.mail.wordpressmail.BaseTest
import com.ritense.mail.wordpressmail.domain.EmailSendRequest
import com.ritense.mail.wordpressmail.domain.EmailSendResponse
import com.ritense.mail.wordpressmail.domain.EmailTemplateResponse
import com.ritense.mail.wordpressmail.service.WordpressMailClient
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

class WordpressMailConnectorTest : BaseTest() {
    lateinit var wordpressMailConnectorProperties: WordpressMailConnectorProperties
    lateinit var wordpressMailClient: WordpressMailClient
    lateinit var wordpressMailConnector: WordpressMailConnector
    lateinit var documentService: DocumentService

    @BeforeEach
    fun setup() {
        super.baseSetUp()
        wordpressMailConnectorProperties = WordpressMailConnectorProperties("http://localhost:8012/")
        wordpressMailClient = mock(WordpressMailClient::class.java)
        documentService = mock(DocumentService::class.java)
        wordpressMailConnector = WordpressMailConnector(
            wordpressMailConnectorProperties = wordpressMailConnectorProperties,
            wordpressMailClient = wordpressMailClient,
            applicationEventPublisher = mock(ApplicationEventPublisher::class.java)
        )
    }

    @Test
    fun `should get properties`() {
        //when
        val properties = wordpressMailConnector.getProperties()
        //then
        assertThat(properties).isNotNull
    }

    @Test
    fun `should set properties`() {

        //when
        wordpressMailConnector.setProperties(wordpressMailConnectorProperties)
        val properties = wordpressMailConnector.getProperties()

        //then
        assertThat(properties).isNotNull
        assertThat(properties).isInstanceOf(WordpressMailConnectorProperties::class.java)
        assertThat((properties as WordpressMailConnectorProperties).url).isEqualTo("http://localhost:8012/")
    }

    @Test
    fun `should throw NotImplementedException`() {
        val rawMailMessage = rawMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )
        Assertions.assertThrows(NotImplementedError::class.java) {
            wordpressMailConnector.send(rawMailMessage)
        }
    }

    @Test
    fun `should build TemplatedMailMessage and call the send method in the WordpressMailClient`() {
        //Given
        val templateResponse = EmailTemplateResponse.EmailTemplate("generic-mail-template", "generic-mail-template", "", "", emptyList<String>())
        whenever(wordpressMailClient.getEmailTemplates())
            .thenReturn(EmailTemplateResponse(true, "", listOf(templateResponse)))
        whenever(wordpressMailClient.send(anyString(), any(), isNull()))
            .thenReturn(EmailSendResponse(true, "", EmailSendResponse.Data("John Doe <john@example.com>", "", "")))
        val templatedMailMessage =
            templatedMailMessage("john@example.com", "John Doe", "generic-mail-template", "Welcome", "VOORNAAM", "John")

        //When
        wordpressMailConnector.send(templatedMailMessage)

        //Then
        val captor: ArgumentCaptor<EmailSendRequest> = ArgumentCaptor.forClass(EmailSendRequest::class.java)
        verify(wordpressMailClient).send(eq("generic-mail-template"), capture(captor), isNull())
        assertThat(captor.value.variables).containsEntry("VOORNAAM", "John")
        assertThat(captor.value.variables).containsEntry("SUBJECT", "Welcome")
        assertThat(captor.value.to).isEqualTo("John Doe <john@example.com>")
    }

}