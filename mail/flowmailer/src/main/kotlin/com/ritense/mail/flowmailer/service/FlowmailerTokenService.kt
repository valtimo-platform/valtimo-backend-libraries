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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

class FlowmailerTokenService(
    private val flowmailerProperties: FlowmailerProperties,
    private val restTemplate: RestTemplate
) {
    fun getFlowmailerToken(): String? {
        val objectMapper = ObjectMapper()
        val httpHeaders = HttpHeaders()

        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val params = mapOf( //TODO: was MultiValueMap. Is that necessary?
            ("client_id" to flowmailerProperties.clientId),
            ("client_secret" to flowmailerProperties.clientSecret),
            ("grant_type" to "client_credentials")
        )
        val httpEntity = HttpEntity(params, httpHeaders)
        val flowmailerToken = restTemplate.exchange(tokenUrl, HttpMethod.POST, httpEntity, String::class.java)
        val typeRef = object : TypeReference<Map<String, String>>() {}

        return objectMapper.readValue(flowmailerToken.body, typeRef)["access_token"]
    }

    companion object {

        private const val tokenUrl = "https://login.flowmailer.net/oauth/token"
    }
}