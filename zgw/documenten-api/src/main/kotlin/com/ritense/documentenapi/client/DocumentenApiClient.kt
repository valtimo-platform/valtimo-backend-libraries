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

package com.ritense.documentenapi.client

import BestandsdelenResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.domain.FileUploadPart
import com.ritense.documentenapi.event.DocumentDeleted
import com.ritense.documentenapi.event.DocumentInformatieObjectDownloaded
import com.ritense.documentenapi.event.DocumentInformatieObjectViewed
import com.ritense.documentenapi.event.DocumentListed
import com.ritense.documentenapi.event.DocumentStored
import com.ritense.documentenapi.event.DocumentUpdated
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.outbox.OutboxService
import com.ritense.resource.authorization.ResourcePermission
import com.ritense.resource.authorization.ResourcePermissionActionProvider
import com.ritense.zgw.ClientTools
import com.ritense.zgw.ClientTools.Companion.optionalQueryParam
import com.ritense.zgw.Page
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.converter.ResourceHttpMessageConverter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import java.io.InputStream
import java.net.URI
import kotlin.math.min

class DocumentenApiClient(
    private val restClientBuilder: RestClient.Builder,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
    private val platformTransactionManager: PlatformTransactionManager,
    private val authorizationService: AuthorizationService,
    private val authrizationEnabled: Boolean = false,
) {
    fun storeDocument(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        request: CreateDocumentRequest
    ): CreateDocumentResult {
        if (authrizationEnabled) {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    ResourcePermission::class.java,
                    ResourcePermissionActionProvider.CREATE,
                    ResourcePermission()
                )
            )
        }
        val result = restClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("enkelvoudiginformatieobjecten")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body<CreateDocumentResult>()!!

        outboxService.send { DocumentStored(result.url, objectMapper.valueToTree(result)) }
        return result
    }

    fun storeDocumentInParts(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        request: BestandsdelenRequest,
        createDocumentResult: CreateDocumentResult,
        bestandsnaam: String
    ) {
        // Inside the CreateDocumentResult there is an array of bestandsdelen.
        // Each bestandsdeel needs to be sent separately
        // So the documenten api determines the amount (and size) of chunks, not this application.
        logger.info { "Starting upload of file $bestandsnaam in ${createDocumentResult.bestandsdelen.size} chunks" }

        createDocumentResult.bestandsdelen.forEach { bestandsdeel ->
            logger.debug { "Sending chunk #${bestandsdeel.volgnummer} for a size of ${bestandsdeel.omvang} bytes" }

            val body = FileUploadPart(bestandsdeel, request, bestandsnaam)
                .createBody()

            restClient(authentication)
                .put()
                .uri {
                    ClientTools.baseUrlToBuilder(it, baseUrl)
                        .path("bestandsdelen/{uuid}")
                        .build(bestandsdeel.url.substring(bestandsdeel.url.lastIndexOf("/") + 1))
                }
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body<BestandsdelenResult>()!!
        }

        check(request.inhoud.read() == -1) {
            "Failed to upload the full file. The file is larger than the sum of the omvang of all bestandsdelen."
        }
    }

    fun getInformatieObject(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        objectId: String,
    ): DocumentInformatieObject {
        return getInformatieObject(authentication, toObjectUrl(baseUrl, objectId))
    }

    fun getInformatieObject(
        authentication: DocumentenApiAuthentication,
        objectUrl: URI
    ): DocumentInformatieObject {
        val result = restClient(authentication)
            .get()
            .uri(objectUrl)
            .retrieve()
            .body<DocumentInformatieObject>()!!

        if (authrizationEnabled) {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    ResourcePermission::class.java,
                    ResourcePermissionActionProvider.VIEW_LIST,
                    ResourcePermission()
                )
            )
        }

        outboxService.send {
            DocumentInformatieObjectViewed(
                result.url.toString(),
                objectMapper.valueToTree(result)
            )
        }

        return result
    }

    fun getInformatieObjecten(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        pageable: Pageable,
        documentSearchRequest: DocumentSearchRequest,
    ): org.springframework.data.domain.Page<DocumentInformatieObject> {
        // because the documenten api only supports a fixed page size, we will try to calculate the page we need to request
        // the only page sizes that are supported are those that can fit n times in the itemsPerPage
        require(ITEMS_PER_PAGE % pageable.pageSize == 0) { "Page size is not supported" }
        requireNotNull(documentSearchRequest.zaakUrl) { "Zaak URL is required" }

        if (authrizationEnabled && !authorizationService.hasPermission(
            EntityAuthorizationRequest(
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.VIEW_LIST,
                ResourcePermission()
            )
        )) {
            return org.springframework.data.domain.Page.empty(pageable)
        }

        val pageToRequest = ((pageable.pageSize * pageable.pageNumber) / ITEMS_PER_PAGE) + 1
        val result =
            restClient(authentication)
                .get()
                .uri {
                    ClientTools.baseUrlToBuilder(it, baseUrl)
                        .path("enkelvoudiginformatieobjecten")
                        .optionalQueryParam("titel", documentSearchRequest.titel)
                        .optionalQueryParam("informatieobjecttype", documentSearchRequest.informatieobjecttype)
                        .optionalQueryParam(
                            "vertrouwelijkheidaanduiding",
                            documentSearchRequest.vertrouwelijkheidaanduiding
                        )
                        .optionalQueryParam("auteur", documentSearchRequest.auteur)
                        .optionalQueryParam("creatiedatum__gte", documentSearchRequest.creatiedatumFrom)
                        .optionalQueryParam("creatiedatum__lte", documentSearchRequest.creatiedatumTo)
                        .optionalQueryParam("trefwoorden", documentSearchRequest.trefwoorden?.joinToString(","))
                        .queryParam("objectinformatieobjecten__object", documentSearchRequest.zaakUrl)
                        .queryParam("page", pageToRequest)
                        .addSortParameter(pageable)
                        .build()
                }
                .retrieve()
                .body<Page<DocumentInformatieObject>>()!!

        // Fix issue where open-zaak responds contains duplicate results
        val results = result.results.filter { documentInformatieObject ->
            result.results.none {
                it.url == documentInformatieObject.url
                    && it.versie != null
                    && documentInformatieObject.versie != null
                    && it.versie > documentInformatieObject.versie
            }
        }

        // trying to find the chunk of the returned page that we need
        val fromIndex = (pageable.pageSize * (pageable.pageNumber)) % ITEMS_PER_PAGE
        val toIndex = fromIndex + pageable.pageSize
        val pageItems = if (fromIndex > results.size) {
            emptyList()
        } else {
            results.subList(fromIndex, min(results.size, toIndex))
        }

        val returnedPage = PageImpl(pageItems, pageable, results.size.toLong())

        outboxService.send {
            DocumentListed(
                objectMapper.valueToTree(pageItems)
            )
        }

        return returnedPage
    }

    fun downloadInformatieObjectContent(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        objectId: String,
    ) = downloadInformatieObjectContent(
        authentication,
        toObjectUrl(baseUrl, objectId)
    )

    fun downloadInformatieObjectContent(
        authentication: DocumentenApiAuthentication,
        objectUrl: URI
    ): InputStream {
        if (authrizationEnabled) {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    ResourcePermission::class.java,
                    ResourcePermissionActionProvider.VIEW,
                    ResourcePermission()
                )
            )
        }

        val result = restClient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, objectUrl)
                    .pathSegment("download")
                    .build()
            }
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .body<Resource>()!!

        TransactionTemplate(platformTransactionManager).executeWithoutResult {
            outboxService.send {
                DocumentInformatieObjectDownloaded(
                    objectUrl.toString()
                )
            }
        }
        return result.inputStream
    }

    fun lockInformatieObject(
        authentication: DocumentenApiAuthentication,
        objectUrl: URI
    ): DocumentLock {
        val result = restClient(authentication)
            .post()
            .uri("$objectUrl/lock")
            .retrieve()
            .body<DocumentLock>()!!
        return result
    }

    fun unlockInformatieObject(
        authentication: DocumentenApiAuthentication,
        objectUrl: URI,
        documentLock: DocumentLock,
    ) {
        restClient(authentication)
            .post()
            .uri("$objectUrl/unlock")
            .contentType(MediaType.APPLICATION_JSON)
            .body(documentLock)
            .retrieve()
            .toBodilessEntity()
    }

    fun deleteInformatieObject(authentication: DocumentenApiAuthentication, url: URI) {
        if (authrizationEnabled) {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    ResourcePermission::class.java,
                    ResourcePermissionActionProvider.DELETE,
                    ResourcePermission()
                )
            )
        }

        restClient(authentication)
            .delete()
            .uri(url)
            .retrieve()
            .toBodilessEntity()

        outboxService.send { DocumentDeleted(url.toASCIIString()) }
    }

    fun modifyInformatieObject(
        authentication: DocumentenApiAuthentication,
        documentUrl: URI,
        patchDocumentRequest: PatchDocumentRequest
    ): DocumentInformatieObject {

        if (authrizationEnabled) {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    ResourcePermission::class.java,
                    ResourcePermissionActionProvider.MODIFY,
                    ResourcePermission()
                )
            )
        }

        runWithoutAuthorization {
            check(getInformatieObject(authentication, documentUrl).status != DocumentStatusType.DEFINITIEF) {
                "InformatieObject ${documentUrl.path.substringAfterLast("/")} with status 'definitief' cannot be updated!"
            }
        }

        val result = restClient(authentication)
            .patch()
            .uri(documentUrl)
            .body(patchDocumentRequest)
            .retrieve()
            .body<DocumentInformatieObject>()!!

        outboxService.send {
            DocumentUpdated(result.url.toASCIIString(), objectMapper.valueToTree(result))
        }
        return result
    }

    private fun toObjectUrl(baseUrl: URI, objectId: String): URI {
        return UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("enkelvoudiginformatieobjecten", objectId)
            .build()
            .toUri()
    }

    private fun restClient(authentication: DocumentenApiAuthentication): RestClient {
        return restClientBuilder
            .clone()
            .apply {
                authentication.applyAuth(it)
            }
            .messageConverters {
                it + ResourceHttpMessageConverter(true)
            }
            .build()
    }

    fun UriBuilder.addSortParameter(pageable: Pageable): UriBuilder {
        val sortString = pageable.sort.map {
            val property = DocumentenApiColumnKey.fromProperty(it.property)
                ?: throw IllegalArgumentException("Unknown Documenten API property ${it.property}")
            val directionMarker = if (it.isAscending) "" else "-"
            "$directionMarker${property.name.lowercase()}"
        }.joinToString(",")
        if (sortString.isNotBlank()) {
            this.queryParam("ordering", sortString)
        }
        return this
    }

    companion object {
        const val ITEMS_PER_PAGE = 100
        val logger = KotlinLogging.logger {}
    }
}
