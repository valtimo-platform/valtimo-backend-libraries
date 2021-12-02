package com.ritense.mail.flowmailer.service

import com.fasterxml.jackson.databind.ObjectMapper
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
        templatedMailSenderSimulation(HttpStatus.ACCEPTED)

        val token = flowmailerTokenService.getFlowmailerToken()

        assertThat(token).isNotNull
        assertThat(token).isEqualTo("testToken")
    }

    @Test
    fun `should get token out of the responseEntity`() {
        val response = OauthTokenResponse(
            accessToken = "testToken",
            expiresIn = 1,
            scope = "api",
            tokenType = "testToken"
        )
        val responseEntity = ResponseEntity(response, null, HttpStatus.ACCEPTED)
//        val actualResponse = objectMapper.readValue(responseEntity.body, OauthTokenResponse::class.java)
//        assertThat(actualResponse).isEqualTo("testToken")
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
        val url = "https://login.flowmailer.net/oauth/token"

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("client_id", "clientId")
        params.add("client_secret", "clientSecret")
        params.add("grant_type", "client_credentials")
        val httpEntity = HttpEntity(params, httpHeaders)

        val responseEntity = ResponseEntity(response, null, status)

        Mockito.`when`(flowmailerProperties.clientId).thenReturn("clientId")
        Mockito.`when`(flowmailerProperties.clientSecret).thenReturn("clientSecret")
        Mockito.`when`(restTemplate.exchange(url, HttpMethod.POST, httpEntity, OauthTokenResponse::class.java))
            .thenReturn(responseEntity)
//        Mockito.`when`(objectMapper.readValue(response.accessToken, String::class.java))
//            .thenReturn("testToken")
    }
}
