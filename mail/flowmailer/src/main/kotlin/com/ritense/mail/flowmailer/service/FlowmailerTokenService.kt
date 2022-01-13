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

import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.domain.OauthTokenResponse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

class FlowmailerTokenService(
    private val flowmailerProperties: FlowmailerProperties,
    private val restTemplate: RestTemplate
) {

    fun getToken(): String {
        try {
            val httpHeaders = HttpHeaders()
            httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

            val params = LinkedMultiValueMap<String, String>()
            params.add("client_id", flowmailerProperties.clientId)
            params.add("client_secret", flowmailerProperties.clientSecret)
            params.add("grant_type", "client_credentials")

            val httpEntity = HttpEntity(params, httpHeaders)
            val response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                httpEntity,
                getType(OauthTokenResponse::class.java)
            )
            return response.body.accessToken
        } catch (e: HttpStatusCodeException) {
            throw HttpClientErrorException(e.statusCode, "No token received")
        }
    }

    fun <T> getType(responseClass: Class<out T>): ParameterizedTypeReference<T> {
        val type: ParameterizedTypeReference<T> = ParameterizedTypeReference.forType(
            ResolvableType.forClass(responseClass).type
        )
        return type
    }

    companion object {
        private const val TOKEN_URL = "https://login.flowmailer.net/oauth/token"
    }
}