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
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.domain.OauthTokenResponse
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

class FlowmailerTokenService(
    private val flowmailerProperties: FlowmailerProperties,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    fun getFlowmailerToken(): String? {
        val httpHeaders = HttpHeaders()

        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("client_id", flowmailerProperties.clientId)
        params.add("client_secret", flowmailerProperties.clientSecret)
        params.add("grant_type", "client_credentials")

        val httpEntity = HttpEntity(params, httpHeaders)
        val response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            httpEntity,
            OauthTokenResponse::class.java
        )
        if (response.statusCode == HttpStatus.OK) {
            val result = objectMapper.convertValue<OauthTokenResponse>(response.body)
            return result.accessToken
        } else {
            throw HttpClientErrorException(
                response.statusCode,
                "No token received"
            )
        }
    }

    companion object {

        private const val tokenUrl = "https://login.flowmailer.net/oauth/token"
    }
}