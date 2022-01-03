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
import com.ritense.mail.flowmailer.domain.OauthTokenResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

class FlowmailerTokenServiceTest : BaseTest() {

    lateinit var flowmailerProperties: FlowmailerProperties
    lateinit var restTemplate: RestTemplate
    lateinit var flowmailerTokenService: FlowmailerTokenService

    @BeforeEach
    internal fun setUp() {
        flowmailerProperties = mock(FlowmailerProperties::class.java)
        restTemplate = mock(RestTemplate::class.java)
        flowmailerTokenService = FlowmailerTokenService(flowmailerProperties, restTemplate)
    }

    @Test
    fun `should return a token`() {
        templatedMailSenderSimulation(HttpStatus.OK)

        val token = flowmailerTokenService.getToken()

        assertThat(token).isNotNull
        assertThat(token).isEqualTo("testToken")
    }

    @Test
    fun `should throw exception when no token is returned`() {
        templatedMailSenderErrorSimulation(HttpStatus.BAD_REQUEST)

        assertThrows(HttpClientErrorException::class.java) {
            flowmailerTokenService.getToken()
        }
    }

    private fun templatedMailSenderSimulation(status: HttpStatus) {
        val response = OauthTokenResponse(
            accessToken = "testToken",
            expiresIn = 1,
            scope = "api",
            tokenType = "testToken"
        )
        val responseEntity = ResponseEntity(response, null, status)
        `when`(flowmailerProperties.clientId).thenReturn("clientId")
        `when`(flowmailerProperties.clientSecret).thenReturn("clientSecret")
        `when`(
            restTemplate.exchange(
                anyString(),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                any(ParameterizedTypeReference::class.java)
            )
        ).thenReturn(responseEntity)
    }

    private fun templatedMailSenderErrorSimulation(status: HttpStatus) {
        `when`(flowmailerProperties.clientId).thenReturn("clientId")
        `when`(flowmailerProperties.clientSecret).thenReturn("clientSecret")
        `when`(
            restTemplate.exchange(
                anyString(),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                any(ParameterizedTypeReference::class.java)
            )
        ).thenThrow(HttpClientErrorException(status, "Error"))
    }
}
