/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.valtimo.contract.utils.SecurityUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

class IkoClient(
    private val restClientBuilder: RestClient.Builder,
) {
    fun getById(
        baseUrl: URI,
        endpointPath: String,
        id: String,
    ): JsonNode {
        try {
            val result = restClientBuilder
                .clone()
                .build()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme(baseUrl.scheme)
                        .host(baseUrl.host)
                        .path(baseUrl.path)
                        .port(baseUrl.port)
                        .pathSegment("endpoints")
                        .path(endpointPath)
                        .pathSegment(id)
                        .build()
                }
                .header(AUTHORIZATION, "Bearer ${SecurityUtils.getCurrentJwtTokenValue()}")
                .retrieve()
                .body<JsonNode>()!!

            return result
        } catch (e: Exception) {
            logger.error { e }
            return jacksonObjectMapper().createObjectNode()
        }
    }

    fun search(
        baseUrl: URI,
        endpointPath: String,
        filters: Map<String, String>,
    ): JsonNode {
        try {
            val result = restClientBuilder
                .clone()
                .build()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme(baseUrl.scheme)
                        .host(baseUrl.host)
                        .path(baseUrl.path)
                        .port(baseUrl.port)
                        .pathSegment("endpoints")
                        .path(endpointPath)
                        .queryParams(
                            LinkedMultiValueMap(
                                filters
                                    .map { (key, value) -> key to listOf(value) }
                                    .associate { it })
                        )
                        .build()
                }
                .header(AUTHORIZATION, "Bearer ${SecurityUtils.getCurrentJwtTokenValue()}")
                .retrieve()
                .body<JsonNode>()!!

            return result
        } catch (e: Exception) {
            logger.error { e }
            return jacksonObjectMapper().createArrayNode()
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
