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

package com.ritense.catalogiapi.client

import com.ritense.catalogiapi.CatalogiApiAuthentication
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Roltype
import com.ritense.catalogiapi.domain.ZaaktypeInformatieobjecttype
import com.ritense.zgw.ClientTools
import com.ritense.zgw.Page
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import java.net.URI

class CatalogiApiClient(
    val webclient: WebClient
) {
    fun getZaaktypeInformatieobjecttypes(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: ZaaktypeInformatieobjecttypeRequest
    ): Page<ZaaktypeInformatieobjecttype> {
        val result = webclient
            .mutate()
            .filter(authentication)
            .build()
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("zaaktype-informatieobjecttypen")
                    .addOptionalQueryParamFromRequest("zaaktype", request.zaaktype)
                    .addOptionalQueryParamFromRequest("informatieobjecttype", request.informatieobjecttype)
                    .addOptionalQueryParamFromRequest("richting", request.richting?.getSearchValue())
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }
            .retrieve()
            .toEntity(ClientTools.getTypedPage(ZaaktypeInformatieobjecttype::class.java))
            .block()

        return result?.body!!
    }

    fun getInformatieobjecttype(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        informatieobjecttypeUrl: URI
    ): Informatieobjecttype {
        if (baseUrl.host != informatieobjecttypeUrl.host)
            throw IllegalArgumentException(
                "Requested informatieobjecttypeUrl '$informatieobjecttypeUrl' is not valid for baseUrl '$baseUrl'"
            )

        val result = webclient
            .mutate()
            .filter(authentication)
            .build()
            .get()
            .uri(informatieobjecttypeUrl)
            .retrieve()
            .toEntity(Informatieobjecttype::class.java)
            .block()

        return result?.body!!
    }

    fun getRoltypen(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: RoltypeRequest,
    ): Page<Roltype> {
        if (baseUrl.host != request.zaaktype?.host) {
            throw IllegalArgumentException(
                "Requested zaakTypeUrl '${request.zaaktype}' is not valid for baseUrl '$baseUrl'"
            )
        }

        val result = webclient
            .mutate()
            .filter(authentication)
            .build()
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("roltypen")
                    .addOptionalQueryParamFromRequest("zaaktype", request.zaaktype)
                    .addOptionalQueryParamFromRequest("omschrijvingGeneriek", request.omschrijvingGeneriek)
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }.retrieve()
            .toEntity(ClientTools.getTypedPage(Roltype::class.java))
            .block()

        return result?.body!!
    }

    private fun UriBuilder.addOptionalQueryParamFromRequest(name: String, value: Any?): UriBuilder {
        if (value != null)
            this.queryParam(name, value.toString())
        return this
    }
}
