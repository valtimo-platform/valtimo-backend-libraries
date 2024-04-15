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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.OutboxService
import com.ritense.zakenapi.ZakenApiAuthentication
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.CreateZaakResponse
import com.ritense.zakenapi.domain.CreateZaakResultaatRequest
import com.ritense.zakenapi.domain.CreateZaakResultaatResponse
import com.ritense.zakenapi.domain.CreateZaakStatusRequest
import com.ritense.zakenapi.domain.CreateZaakStatusResponse
import com.ritense.zakenapi.domain.PatchZaakRequest
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.domain.ZaakStatus
import com.ritense.zakenapi.domain.ZaakopschortingRequest
import com.ritense.zakenapi.domain.ZaakopschortingResponse
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.event.DocumentLinkedToZaak
import com.ritense.zakenapi.event.ZaakCreated
import com.ritense.zakenapi.event.ZaakInformatieObjectenListed
import com.ritense.zakenapi.event.ZaakObjectenListed
import com.ritense.zakenapi.event.ZaakOpschortingUpdated
import com.ritense.zakenapi.event.ZaakPatched
import com.ritense.zakenapi.event.ZaakResultaatCreated
import com.ritense.zakenapi.event.ZaakRolCreated
import com.ritense.zakenapi.event.ZaakRollenListed
import com.ritense.zakenapi.event.ZaakStatusCreated
import com.ritense.zakenapi.event.ZaakStatusViewed
import com.ritense.zakenapi.event.ZaakViewed
import com.ritense.zgw.ClientTools
import com.ritense.zgw.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

class ZakenApiClient(
    private val webclientBuilder: WebClient.Builder,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper
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

        if (result.hasBody()) {
            outboxService.send {
                DocumentLinkedToZaak(
                    result.body.uuid,
                    objectMapper.valueToTree(result.body)
                )
            }
        }

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


        if (result.hasBody()) {
            outboxService.send {
                ZaakObjectenListed(
                    objectMapper.valueToTree(result.body.results)
                )
            }
        }

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

        if (result.hasBody()) {
            outboxService.send {
                ZaakInformatieObjectenListed(
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    fun getZaakRollen(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI,
        page: Int,
        roleType: RolType? = null
    ): Page<Rol> {
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
                        if (roleType != null) {
                            queryParam("omschrijvingGeneriek", roleType.getApiValue())
                        }
                    }
                    .build()
            }
            .retrieve()
            .toEntity(ClientTools.getTypedPage(Rol::class.java))
            .block()

        if (result.hasBody()) {
            outboxService.send {
                ZaakRollenListed(
                    objectMapper.valueToTree(result.body.results)
                )
            }
        }

        return result?.body!!
    }

    fun createZaakRol(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        rol: Rol
    ): Rol {
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

        if (result.hasBody()) {
            outboxService.send {
                ZaakRolCreated(
                    result.body.url.toString(),
                    objectMapper.valueToTree(result.body)
                )
            }
        }

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

        if (result.hasBody()) {
            outboxService.send {
                ZaakCreated(
                    result.body.url.toString(),
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    fun patchZaak(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: PatchZaakRequest,
    ): ZaakResponse {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .patch()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaken")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(ZaakResponse::class.java)
            .block()

        if (result.hasBody()) {
            outboxService.send {
                ZaakPatched(
                    result.body.url.toString(),
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    fun createZaakStatus(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: CreateZaakStatusRequest,
    ): CreateZaakStatusResponse {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("statussen")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(CreateZaakStatusResponse::class.java)
            .block()

        if (result.hasBody()) {
            outboxService.send {
                ZaakStatusCreated(
                    result.body.url.toString(),
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    fun getZaakStatus(
        authentication: ZakenApiAuthentication,
        zaakStatusUrl: URI,
    ): ZaakStatus {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri(zaakStatusUrl)
            .retrieve()
            .toEntity(ZaakStatus::class.java)
            .block()

        if (result.hasBody()) {
            outboxService.send {
                ZaakStatusViewed(
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    fun createZaakResultaat(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: CreateZaakResultaatRequest,
    ): CreateZaakResultaatResponse {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("resultaten")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(CreateZaakResultaatResponse::class.java)
            .block()

        if (result.hasBody()) {
            outboxService.send {
                ZaakResultaatCreated(
                    result.body.url.toString(),
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    fun setZaakOpschorting(
        authentication: ZakenApiAuthentication,
        url: URI,
        request: ZaakopschortingRequest,
    ): ZaakopschortingResponse {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .patch()
            .uri { url }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(ZaakopschortingResponse::class.java)
            .block()

        if (result.hasBody()) {
            outboxService.send {
                ZaakOpschortingUpdated(
                    result.body.url,
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    fun getZaak(authentication: ZakenApiAuthentication, zaakUrl: URI): ZaakResponse {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri(zaakUrl)
            .headers(this::defaultHeaders)
            .retrieve()
            .toEntity(ZaakResponse::class.java)
            .block()

        if (result.hasBody()) {
            outboxService.send {
                ZaakViewed(
                    result.body.url.toString(),
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
    }

    private fun defaultHeaders(headers: HttpHeaders) {
        headers.set("Accept-Crs", "EPSG:4326")
        headers.set("Content-Crs", "EPSG:4326")
    }
}
