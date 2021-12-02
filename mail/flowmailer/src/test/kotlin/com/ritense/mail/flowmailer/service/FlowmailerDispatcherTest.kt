/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.mail.flowmailer.service

import com.ritense.mail.flowmailer.BaseTest
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.domain.SubmitMessage
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.apache.commons.lang3.NotImplementedException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class FlowmailerDispatcherTest : BaseTest() {

    @Mock
    lateinit var flowmailerProperties: FlowmailerProperties

    @Mock
    lateinit var flowmailerTokenService: FlowmailerTokenService

    @Mock
    lateinit var restTemplate: RestTemplate

    @InjectMocks
    lateinit var flowmailerMailDispatcher: FlowmailerMailDispatcher

    @Test
    fun `should throw NotImplementedException`() {
        val rawMailMessage = rawMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )

        val exception = assertThrows(NotImplementedException::class.java) {
            flowmailerMailDispatcher.send(rawMailMessage)
        }
        assertThat(exception).hasMessageContaining("Send has not been implemented with RawMailMessage")
    }

    @Test
    fun `should send templatedMail and receive mailMessageStatus`() {
        val templatedMailMessage = templatedMailSenderSimulation(HttpStatus.ACCEPTED)

        val mailMessageStatus = flowmailerMailDispatcher.send(templatedMailMessage)
        assertThat(mailMessageStatus).isNotNull
        assertThat(mailMessageStatus[0]).isInstanceOf(MailMessageStatus::class.java)
        assertThat(mailMessageStatus[0].status).isEqualTo("SENT")
    }

    @Test
    fun `should throw HttpClientErrorException when 4XX`() {
        val templatedMailMessage = templatedMailSenderSimulation(HttpStatus.BAD_REQUEST)
        val exception = assertThrows(HttpClientErrorException::class.java) {
            flowmailerMailDispatcher.send(templatedMailMessage)
        }
        assertThat(exception).hasMessageContaining("Message has not been sent due to client side error")
    }

    @Test
    fun `should throw HttpServerErrorException when 5XX`() {
        val templatedMailMessage = templatedMailSenderSimulation(HttpStatus.SERVICE_UNAVAILABLE)
        val exception = assertThrows(HttpServerErrorException::class.java) {
            flowmailerMailDispatcher.send(templatedMailMessage)
        }
        assertThat(exception).hasMessageContaining("Message has not been sent due to server side error")
    }

    @Test
    fun `should return maxSizeAttachments`() {
        val actual = flowmailerMailDispatcher.getMaximumSizeAttachments()
        assertThat(actual).isEqualTo(16250000)
    }

    private fun templatedMailSenderSimulation(status: HttpStatus): TemplatedMailMessage {
        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )
        val message = SubmitMessage.from(templatedMailMessage).first()
        val url = "https://api.flowmailer.net/accountId/messages/submit"
        val httpEntity = HttpEntity(message.toString(), getHttpHeaders())
        val responseEntity = ResponseEntity<String>(status)

        `when`(flowmailerProperties.accountId).thenReturn("accountId")
        `when`(flowmailerTokenService.getFlowmailerToken()).thenReturn("token")
        `when`(restTemplate.exchange(url, HttpMethod.POST, httpEntity, String::class.java)).thenReturn(responseEntity)

        return templatedMailMessage
    }
}