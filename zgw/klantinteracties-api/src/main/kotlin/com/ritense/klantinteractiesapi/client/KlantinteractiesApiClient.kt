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

package com.ritense.klantinteractiesapi.client

import com.ritense.klantinteractiesapi.KlantinteractiesApiAuthentication
import com.ritense.klantinteractiesapi.client.dto.CreatePartijRequest
import com.ritense.klantinteractiesapi.client.dto.GetPartijenRequest
import com.ritense.klantinteractiesapi.domain.Partij
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zgw.ClientTools
import com.ritense.zgw.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriBuilder
import java.net.URI

@SkipComponentScan
@Component
class KlantinteractiesApiClient(
    private val restClientBuilder: RestClient.Builder
) {
    fun getParijen(
        authentication: KlantinteractiesApiAuthentication,
        baseUrl: URI,
        request: GetPartijenRequest,
        pageable: Pageable = Pageable.ofSize(100),
    ): Page<Partij> {
        return restClientBuilder
            .clone()
            .apply { authentication.applyAuth(it) }
            .build()
            .get()
            .uri {
                var builder = ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("partijen")

                request.filters?.forEach { key, value ->
                    builder = builder.addOptionalQueryParamFromRequest(key, value)
                }

                builder
                    .addOptionalQueryParamFromRequest("indicatieActief", request.indicatieActief)
                    .addOptionalQueryParamFromRequest(
                        "partijIdentificator__codeObjecttype",
                        request.partijIdentificator?.codeObjecttype
                    )
                    .addOptionalQueryParamFromRequest(
                        "partijIdentificator__codeRegister",
                        request.partijIdentificator?.codeRegister
                    )
                    .addOptionalQueryParamFromRequest(
                        "partijIdentificator__codeSoortObjectId",
                        request.partijIdentificator?.codeSoortObjectId
                    )
                    .addOptionalQueryParamFromRequest(
                        "partijIdentificator__objectId",
                        request.partijIdentificator?.objectId
                    )
                    .addOptionalQueryParamFromRequest("soortPartij", request.soortPartij?.key)
                    .addOptionalQueryParamFromRequest("page", pageable.pageNumber + 1)
                    .addOptionalQueryParamFromRequest("pageSize", pageable.pageSize)
                    .build()
            }
            .retrieve()
            .body<Page<Partij>>()!!
    }

    fun getParij(
        authentication: KlantinteractiesApiAuthentication,
        baseUrl: URI,
        partijUrl: URI,
    ): Partij {
        require(partijUrl.toString().startsWith(baseUrl.toString())) {
            "partijUrl '$partijUrl' does not start with baseUrl '$baseUrl'"
        }
        return restClientBuilder
            .clone()
            .apply { authentication.applyAuth(it) }
            .build()
            .get()
            .uri(partijUrl)
            .retrieve()
            .body<Partij>()!!
    }

    fun createPartij(
        authentication: KlantinteractiesApiAuthentication,
        baseUrl: URI,
        request: CreatePartijRequest
    ): Partij {
        return restClientBuilder
            .clone()
            .apply { authentication.applyAuth(it) }
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("partijen")
                    .build()
            }
            .contentType(APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<Partij>()!!
    }

    private fun UriBuilder.addOptionalQueryParamFromRequest(name: String, value: Any?): UriBuilder {
        if (value != null)
            this.queryParam(name, value.toString())
        return this
    }
}