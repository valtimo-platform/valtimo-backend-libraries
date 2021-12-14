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
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.apache.commons.lang3.NotImplementedException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

class FlowmailerDispatcherTest : BaseTest() {
    lateinit var flowmailerProperties: FlowmailerProperties
    lateinit var flowmailerTokenService: FlowmailerTokenService
    lateinit var restTemplate: RestTemplate
    lateinit var flowmailerMailDispatcher: FlowmailerMailDispatcher

    @BeforeEach
    internal fun setUp() {
        flowmailerProperties = mock(FlowmailerProperties::class.java)
        flowmailerTokenService = mock(FlowmailerTokenService::class.java)
        restTemplate = mock(RestTemplate::class.java)
        flowmailerMailDispatcher = FlowmailerMailDispatcher(
            flowmailerProperties,
            flowmailerTokenService,
            restTemplate,
            Mapper.INSTANCE.get()
        )
    }

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
        templatedMailSenderSimulation(HttpStatus.ACCEPTED)

        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )

        val mailMessageStatus = flowmailerMailDispatcher.send(templatedMailMessage)
        assertThat(mailMessageStatus).isNotNull
        assertThat(mailMessageStatus[0]).isInstanceOf(MailMessageStatus::class.java)
        assertThat(mailMessageStatus[0].status).isEqualTo("SENT")
        assertThat(mailMessageStatus[0].email.toString()).isEqualTo(templatedMailMessage.recipients.get().first().email.get())
    }

    @Test
    fun `should throw HttpClientErrorException when 4XX`() {
        templatedMailSenderErrorSimulation(HttpStatus.BAD_REQUEST)

        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )
        assertThrows(HttpClientErrorException::class.java) {
            flowmailerMailDispatcher.send(templatedMailMessage)
        }
    }

    @Test
    fun `should throw HttpServerErrorException when 5XX`() {
        templatedMailSenderErrorSimulation(HttpStatus.SERVICE_UNAVAILABLE)

        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )
        assertThrows(HttpServerErrorException::class.java) {
            flowmailerMailDispatcher.send(templatedMailMessage)
        }
    }

    @Test
    fun `should return maxSizeAttachments`() {
        val actual = flowmailerMailDispatcher.getMaximumSizeAttachments()
        assertThat(actual).isEqualTo(FlowmailerMailDispatcher.MAX_SIZE_ATTACHMENTS_IN_BYTES)
    }

    private fun templatedMailSenderSimulation(status: HttpStatus) {
        val location: MultiValueMap<String, String> = LinkedMultiValueMap()
        location.add("Location", "https://api.flowmailer.net/520/messages/202106110944460bfd0ca81fd281ef9e")
        val responseEntity = ResponseEntity<String>(location, status)

        `when`(flowmailerProperties.accountId).thenReturn("accountId")
        `when`(flowmailerTokenService.getToken()).thenReturn("token")
        `when`(restTemplate.exchange(
            anyString(),
            any(HttpMethod::class.java),
            any(HttpEntity::class.java),
            eq(String::class.java),
        )).thenReturn(responseEntity)
    }

    private fun templatedMailSenderErrorSimulation(status: HttpStatus) {
        `when`(flowmailerProperties.accountId).thenReturn("accountId")
        `when`(flowmailerTokenService.getToken()).thenReturn("token")
        `when`(restTemplate.exchange(
            anyString(),
            any(HttpMethod::class.java),
            any(HttpEntity::class.java),
            eq(String::class.java),
        )).thenThrow(HttpClientErrorException(status, "Error"))
    }

}