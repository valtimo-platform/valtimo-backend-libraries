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

package com.ritense.zakenapi.client

import com.ritense.zakenapi.ZakenApiAuthentication
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.CreateZaakResponse
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zgw.ClientTools
import com.ritense.zgw.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

class ZakenApiClient(
    private val webclientBuilder: WebClient.Builder
) {
    fun linkDocument(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: LinkDocumentRequest
    ): LinkDocumentResult {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaakinformatieobjecten")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(LinkDocumentResult::class.java)
            .block()

        return result?.body!!
    }

    fun getZaakObjecten(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI,
        page: Int
    ): Page<ZaakObject> {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaakobjecten")
                    .queryParam("page", page)
                    .queryParam("zaak", zaakUrl)
                    .build()
            }
            .retrieve()
            .toEntity(ClientTools.getTypedPage(ZaakObject::class.java))
            .block()

        return result?.body!!
    }

    fun getZaakInformatieObjecten(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI
    ): List<ZaakInformatieObject> {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaakinformatieobjecten")
                    .queryParam("zaak", zaakUrl)
                    .build()
            }
            .retrieve()
            .toEntityList(ZaakInformatieObject::class.java)
            .block()

        return result?.body!!
    }

    fun getZaakRollen(authentication: ZakenApiAuthentication,
                      baseUrl: URI,
                      zaakUrl: URI,
                      page: Int,
                      roleType: RolType? = null): Page<Rol> {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("rollen")
                    .queryParam("page", page)
                    .queryParam("zaak", zaakUrl)
                    .apply {
                        if(roleType != null) {
                            queryParam("omschrijvingGeneriek", roleType.getApiValue())
                        }
                    }
                    .build()
            }
            .retrieve()
            .toEntity(ClientTools.getTypedPage(Rol::class.java))
            .block()

        return result?.body!!
    }

    fun createZaakRol(authentication: ZakenApiAuthentication,
                      baseUrl: URI,
                      rol: Rol): Rol {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("rollen")
                    .build()
            }
            .body(BodyInserters.fromValue(rol))
            .retrieve()
            .toEntity(Rol::class.java)
            .block()

        return result?.body!!
    }

    fun createZaak(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: CreateZaakRequest,
    ): CreateZaakResponse {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaken")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(CreateZaakResponse::class.java)
            .block()

        return result?.body!!
    }

    private fun defaultHeaders(headers: HttpHeaders) {
        headers.set("Accept-Crs", "EPSG:4326")
        headers.set("Content-Crs", "EPSG:4326")
    }
}
