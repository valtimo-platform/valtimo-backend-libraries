/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.besluit.client

import com.ritense.besluit.connector.BesluitProperties
import com.ritense.besluit.domain.Besluit
import com.ritense.besluit.domain.BesluitInformatieobjectRelatie
import com.ritense.besluit.domain.request.BesluitInformatieobjectRelatieRequest
import com.ritense.besluit.domain.request.CreateBesluitRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

open class BesluitClient(
    private val webclientBuilder: WebClient.Builder,
    private val besluitTokenGenerator: BesluitTokenGenerator
) {
    /**
     * Create a BESLUIT
     *
     * @param request the <code>CreateBesluitRequest</code> to use when createing new requests
     */
    suspend fun createBesluit(request: CreateBesluitRequest, besluitProperties: BesluitProperties): Besluit {
        return webClient(besluitProperties)
            .post()
            .uri("/api/v1/besluiten")
            .bodyValue(request)
            .retrieve()
            .awaitBody<Besluit>()
    }

    /**
     * Create link between Besluit and Informatieobject
     */
    suspend fun createBesluitInformatieobjectRelatie(
        request: BesluitInformatieobjectRelatieRequest,
        besluitProperties: BesluitProperties
    ): BesluitInformatieobjectRelatie {
        return webClient(besluitProperties)
            .post()
            .uri("/api/v1/besluitinformatieobjecten")
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }

    private fun webClient(besluitProperties: BesluitProperties): WebClient {
        val token = besluitTokenGenerator.generateToken(
            besluitProperties.secret,
            besluitProperties.clientId
        )
        return webclientBuilder
            .clone()
            .baseUrl(besluitProperties.url)
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}