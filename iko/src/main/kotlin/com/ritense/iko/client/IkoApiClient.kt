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

package com.ritense.iko.client

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

class IkoApiClient(
    private val restClientBuilder: RestClient.Builder,
) {
    fun get(
        baseUrl: URI,
        dataAggregate: String,
        id: String,
    ): JsonNode {
        val result = restClientBuilder
            .clone()
            .build()
            .get()
            .uri {
                it.scheme(baseUrl.scheme)
                    .host(baseUrl.host)
                    .path(baseUrl.path)
                    .port(baseUrl.port)
                    .pathSegment(dataAggregate)
                    .pathSegment(id)
                    .build()
            }
            .retrieve()
            .body<JsonNode>()!!

        return result
    }

    fun search(
        baseUrl: URI,
        dataAggregate: String,
        searchQuery: String,
    ): JsonNode {
        val result = restClientBuilder
            .clone()
            .build()
            .get()
            .uri {
                it.scheme(baseUrl.scheme)
                    .host(baseUrl.host)
                    .path(baseUrl.path)
                    .port(baseUrl.port)
                    .pathSegment(dataAggregate)
                    .pathSegment(searchQuery)
                    .build()
            }
            .retrieve()
            .body<JsonNode>()!!

        return result
    }

}
