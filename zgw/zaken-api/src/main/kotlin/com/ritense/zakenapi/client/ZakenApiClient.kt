/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.client

import com.ritense.zakenapi.ZakenApiAuthentication
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

class ZakenApiClient(
    val webclient: WebClient
) {
    fun linkDocument(
        authentication: ZakenApiAuthentication,
        baseUrl: String,
        request: LinkDocumentRequest
    ): LinkDocumentResult {
        val result = webclient
            .mutate()
            .filter(authentication)
            .build()
            .post()
            .uri(baseUrl + "zaakinformatieobjecten")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(LinkDocumentResult::class.java)
            .block()

        return result?.body!!
    }
}