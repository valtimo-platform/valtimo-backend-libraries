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

package com.ritense.zakenapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.OutboxService
import com.ritense.valtimo.web.logging.RestClientLoggingExtension
import com.ritense.zakenapi.ZakenApiAuthentication
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.CreateZaakResultaatRequest
import com.ritense.zakenapi.domain.CreateZaakResultaatResponse
import com.ritense.zakenapi.domain.CreateZaakStatusRequest
import com.ritense.zakenapi.domain.CreateZaakStatusResponse
import com.ritense.zakenapi.domain.CreateZaakeigenschapRequest
import com.ritense.zakenapi.domain.PatchZaakRequest
import com.ritense.zakenapi.domain.UpdateZaakeigenschapRequest
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.domain.ZaakResultaat
import com.ritense.zakenapi.domain.ZaakStatus
import com.ritense.zakenapi.domain.ZaakeigenschapResponse
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
import com.ritense.zakenapi.event.ZaakResultaatViewed
import com.ritense.zakenapi.event.ZaakRolCreated
import com.ritense.zakenapi.event.ZaakRollenListed
import com.ritense.zakenapi.event.ZaakStatusCreated
import com.ritense.zakenapi.event.ZaakStatusViewed
import com.ritense.zakenapi.event.ZaakViewed
import com.ritense.zakenapi.event.ZaakeigenschapCreated
import com.ritense.zakenapi.event.ZaakeigenschapDeleted
import com.ritense.zakenapi.event.ZaakeigenschapListed
import com.ritense.zakenapi.event.ZaakeigenschapUpdated
import com.ritense.zgw.ClientTools
import com.ritense.zgw.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

class ZakenApiClient(
    private val restClientBuilder: RestClient.Builder,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper
) {
    fun linkDocument(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: LinkDocumentRequest
    ): LinkDocumentResult {
        val result = buildWebClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaakinformatieobjecten")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<LinkDocumentResult>()

        result?.let {
            outboxService.send { DocumentLinkedToZaak(result.uuid, objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun getZaakObjecten(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI,
        page: Int
    ): Page<ZaakObject> {
        val result = buildWebClient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaakobjecten")
                    .queryParam("page", page)
                    .queryParam("zaak", zaakUrl)
                    .build()
            }
            .retrieve()
            .body<Page<ZaakObject>>()

        result?.let {
            outboxService.send { ZaakObjectenListed(objectMapper.valueToTree(result.results)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun getZaakInformatieObjecten(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI? = null,
        informatieobjectUrl: URI? = null,
    ): List<ZaakInformatieObject> {
        val result = buildWebClient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaakinformatieobjecten").also {
                        zaakUrl?.let { url ->
                            it.queryParam("zaak", url)
                        }
                    }.also {
                        informatieobjectUrl?.let { url ->
                            it.queryParam("informatieobject", url)
                        }
                    }
                    .build()
            }
            .retrieve()
            .body<List<ZaakInformatieObject>>()

        result?.let {
            outboxService.send { ZaakInformatieObjectenListed(objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun getZaakRollen(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI,
        page: Int,
        roleType: RolType? = null
    ): Page<Rol> {
        val result = buildWebClient(authentication)
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
            .body<Page<Rol>>()

        result?.let {
            outboxService.send { ZaakRollenListed(objectMapper.valueToTree(result.results)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun createZaakRol(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        rol: Rol
    ): Rol {
        val result = buildWebClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("rollen")
                    .build()
            }
            .body(rol)
            .retrieve()
            .body<Rol>()

        result?.let {
            outboxService.send {
                ZaakRolCreated(result.url.toString(), objectMapper.valueToTree(result))
            }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun createZaak(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: CreateZaakRequest,
    ): ZaakResponse {
        val result = buildWebClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("zaken")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<ZaakResponse>()

        result?.let {
            outboxService.send { ZaakCreated(result.url.toString(), objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun patchZaak(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI,
        request: PatchZaakRequest,
    ): ZaakResponse {
        validateUrlHost(baseUrl, zaakUrl)
        val result = buildWebClient(authentication)
            .patch()
            .uri(zaakUrl)
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<ZaakResponse>()

        result?.let {
            outboxService.send { ZaakPatched(result.url.toString(), objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun createZaakStatus(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: CreateZaakStatusRequest,
    ): CreateZaakStatusResponse {
        val result = buildWebClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("statussen")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<CreateZaakStatusResponse>()

        result?.let {
            outboxService.send { ZaakStatusCreated(result.url.toString(), objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun getZaakStatus(
        authentication: ZakenApiAuthentication,
        zaakStatusUrl: URI,
    ): ZaakStatus {
        val result = buildWebClient(authentication)
            .get()
            .uri(zaakStatusUrl)
            .retrieve()
            .body<ZaakStatus>()

        result?.let {
            outboxService.send { ZaakStatusViewed(objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun getZaakResultaat(
        authentication: ZakenApiAuthentication,
        zaakResultaatUrl: URI,
    ): ZaakResultaat {
        val result = buildWebClient(authentication)
            .get()
            .uri(zaakResultaatUrl)
            .retrieve()
            .body<ZaakResultaat>()

        result?.let {
            outboxService.send { ZaakResultaatViewed(objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun createZaakResultaat(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: CreateZaakResultaatRequest,
    ): CreateZaakResultaatResponse {
        val result = buildWebClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("resultaten")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<CreateZaakResultaatResponse>()

        result?.let {
            outboxService.send { ZaakResultaatCreated(result.url.toString(), objectMapper.valueToTree(result)) }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun setZaakOpschorting(
        authentication: ZakenApiAuthentication,
        url: URI,
        request: ZaakopschortingRequest,
    ): ZaakopschortingResponse {
        val result = buildWebClient(authentication)
            .patch()
            .uri { url }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<ZaakopschortingResponse>()

        result?.let { outboxService.send { ZaakOpschortingUpdated(result.url, objectMapper.valueToTree(result)) } }
        return result ?: throw IllegalStateException("No result found")
    }

    fun getZaak(authentication: ZakenApiAuthentication, zaakUrl: URI): ZaakResponse {
        val result = buildWebClient(authentication)
            .get()
            .uri(zaakUrl)
            .headers(this::defaultHeaders)
            .retrieve()
            .body<ZaakResponse>()

        result?.let {
            outboxService.send {
                ZaakViewed(
                    result.url.toString(),
                    objectMapper.valueToTree(result)
                )
            }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun createZaakeigenschap(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        request: CreateZaakeigenschapRequest,
    ): ZaakeigenschapResponse {
        validateUrlHost(baseUrl, request.zaak)
        val result = buildWebClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, request.zaak)
                    .pathSegment("zaakeigenschappen")
                    .build()
            }
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<ZaakeigenschapResponse>()

        result?.let {
            outboxService.send {
                ZaakeigenschapCreated(
                    result.url.toString(),
                    objectMapper.valueToTree(result)
                )
            }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun updateZaakeigenschap(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakeigenschapUrl: URI,
        request: UpdateZaakeigenschapRequest,
    ): ZaakeigenschapResponse {
        validateUrlHost(baseUrl, zaakeigenschapUrl)
        validateUrlHost(baseUrl, request.zaak)
        val result = buildWebClient(authentication)
            .put()
            .uri(zaakeigenschapUrl)
            .headers(this::defaultHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<ZaakeigenschapResponse>()

        result?.let {
            outboxService.send {
                ZaakeigenschapUpdated(
                    result.url.toString(),
                    objectMapper.valueToTree(result)
                )
            }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    fun deleteZaakeigenschap(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakeigenschapUrl: URI,
    ) {
        validateUrlHost(baseUrl, zaakeigenschapUrl)
        buildWebClient(authentication)
            .delete()
            .uri(zaakeigenschapUrl)
            .headers(this::defaultHeaders)
            .retrieve()
            .toBodilessEntity()

        outboxService.send {
            ZaakeigenschapDeleted(zaakeigenschapUrl.toString())
        }
    }

    fun getZaakeigenschappen(
        authentication: ZakenApiAuthentication,
        baseUrl: URI,
        zaakUrl: URI,
    ): List<ZaakeigenschapResponse> {
        validateUrlHost(baseUrl, zaakUrl)
        val result = buildWebClient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, zaakUrl)
                    .pathSegment("zaakeigenschappen")
                    .build()
            }
            .retrieve()
            .body<List<ZaakeigenschapResponse>>()

        result?.let {
            outboxService.send {
                ZaakeigenschapListed(objectMapper.valueToTree(result))
            }
        }
        return result ?: throw IllegalStateException("No result found")
    }

    private fun validateUrlHost(baseUrl: URI, url: URI?) {
        if (url != null && baseUrl.host != url.host) {
            throw IllegalArgumentException(
                "Requested url '$url' is not valid for baseUrl '$baseUrl'"
            )
        }
    }

    private fun defaultHeaders(headers: HttpHeaders) {
        headers.set("Accept-Crs", "EPSG:4326")
        headers.set("Content-Crs", "EPSG:4326")
    }

    fun deleteZaakInformatieObject(authentication: ZakenApiAuthentication, baseUrl: URI, zaakInformatieobjectUrl: URI) {
        require(zaakInformatieobjectUrl.toString().startsWith(baseUrl.toString())) {
            "zaakInformatieobjectUrl '$zaakInformatieobjectUrl' does not start with baseUrl '$baseUrl'"
        }
        buildWebClient(authentication)
            .delete()
            .uri(zaakInformatieobjectUrl)
            .retrieve()
            .toBodilessEntity()
    }

    private fun buildWebClient(authentication: ZakenApiAuthentication): RestClient {
        return restClientBuilder
            .clone()
            .apply {
                authentication.bearerAuth(it)
                RestClientLoggingExtension.defaultRequestLogging(it)
            }
            .build()
    }
}
