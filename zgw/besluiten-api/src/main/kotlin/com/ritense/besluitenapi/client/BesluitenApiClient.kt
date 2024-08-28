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

package com.ritense.besluitenapi.client

import com.ritense.besluitenapi.BesluitenApiAuthentication
import com.ritense.valtimo.web.logging.RestClientLoggingExtension
import com.ritense.zgw.ClientTools
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.BodyInserters
import java.net.URI

class BesluitenApiClient(
    private val restClientBuilder: RestClient.Builder
) {
    fun createBesluit(
        authentication: BesluitenApiAuthentication,
        baseUrl: URI,
        request: CreateBesluitRequest
    ): Besluit {
        val result = restClientBuilder
            .clone()
            .apply {
                authentication.bearerAuth(it)
                RestClientLoggingExtension.defaultRequestLogging(it)
            }
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("besluiten")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(Besluit::class.java)
        return result.body!!
    }

    fun createBesluitInformatieObject(
        authentication: BesluitenApiAuthentication,
        url: URI,
        besluitInformatieObject: CreateBesluitInformatieObject
    ): ResponseEntity<BesluitInformatieObject>? {
        return restClientBuilder
            .clone()
            .apply {
                authentication.bearerAuth(it)
                RestClientLoggingExtension.defaultRequestLogging(it)
            }
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, url)
                    .path("besluitinformatieobjecten")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(besluitInformatieObject)
            .retrieve()
            .toEntity(BesluitInformatieObject::class.java)
    }
}