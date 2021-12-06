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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.ritense.mail.flowmailer.BaseTest
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.domain.OauthTokenResponse
import com.ritense.valtimo.contract.json.Mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

class FlowmailerTokenServiceTest : BaseTest() {

    lateinit var flowmailerProperties: FlowmailerProperties
    lateinit var restTemplate: RestTemplate
    lateinit var objectMapper: ObjectMapper
    lateinit var flowmailerTokenService: FlowmailerTokenService

    @BeforeEach
    internal fun setUp() {
        flowmailerProperties = mock(FlowmailerProperties::class.java)
        restTemplate = mock(RestTemplate::class.java)
        objectMapper = Mapper.INSTANCE.get()
        flowmailerTokenService = FlowmailerTokenService(flowmailerProperties, restTemplate, objectMapper)
    }

    @Test
    fun `should return a token`() {
        templatedMailSenderSimulation(HttpStatus.OK)

        val token = flowmailerTokenService.getFlowmailerToken()

        assertThat(token).isNotNull
        assertThat(token).isEqualTo("testToken")
    }

    @Test
    fun `should throw exception when no token is returned`() {
        templatedMailSenderSimulation(HttpStatus.BAD_REQUEST)

        val exception = Assertions.assertThrows(HttpClientErrorException::class.java) {
            flowmailerTokenService.getFlowmailerToken()
        }
        assertThat(exception).hasMessageContaining("No token received")
    }

    private fun templatedMailSenderSimulation(status: HttpStatus) {
        val response = OauthTokenResponse(
            accessToken = "testToken",
            expiresIn = 1,
            scope = "api",
            tokenType = "testToken"
        )
        val responseAsString = objectMapper.writeValueAsString(response)
        val url = "https://login.flowmailer.net/oauth/token"

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("client_id", "clientId")
        params.add("client_secret", "clientSecret")
        params.add("grant_type", "client_credentials")
        val httpEntity = HttpEntity(params, httpHeaders)

        val responseEntity = ResponseEntity(responseAsString, null, status)

        `when`(flowmailerProperties.clientId).thenReturn("clientId")
        `when`(flowmailerProperties.clientSecret).thenReturn("clientSecret")
        `when`(restTemplate.exchange(url, HttpMethod.POST, httpEntity, String::class.java))
            .thenReturn(responseEntity)
    }
}
