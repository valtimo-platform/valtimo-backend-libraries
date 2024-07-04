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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.documentenapi.event.DocumentDeleted
import com.ritense.documentenapi.event.DocumentInformatieObjectDownloaded
import com.ritense.documentenapi.event.DocumentInformatieObjectViewed
import com.ritense.documentenapi.event.DocumentStored
import com.ritense.documentenapi.event.DocumentUpdated
import com.ritense.outbox.OutboxService
import com.ritense.zgw.ClientTools
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URI

class DocumentenApiClient(
    private val webclientBuilder: WebClient.Builder,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
    private val platformTransactionManager: PlatformTransactionManager
) {
    fun storeDocument(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        request: CreateDocumentRequest
    ): CreateDocumentResult {
        val result = buildFilteredClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .path("enkelvoudiginformatieobjecten")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .toEntity(CreateDocumentResult::class.java)
            .block()

        if (result.hasBody()) {
            outboxService.send {
                DocumentStored(
                    result.body.url,
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result?.body!!
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
        val result = checkNotNull(
            buildFilteredClient(authentication)
                .get()
                .uri(objectUrl)
                .retrieve()
                .toEntity(DocumentInformatieObject::class.java)
                .block()?.body
        ) {
            "Could not retrieve ${DocumentInformatieObject::class.simpleName} at $objectUrl"
        }

        outboxService.send {
            DocumentInformatieObjectViewed(
                result.url.toString(),
                objectMapper.valueToTree(result)
            )
        }

        return result
    }

    fun downloadInformatieObjectContent(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        objectId: String,
    ): InputStream {
        return downloadInformatieObjectContent(authentication, toObjectUrl(baseUrl, objectId))
    }

    fun downloadInformatieObjectContent(
        authentication: DocumentenApiAuthentication,
        objectUrl: URI
    ): InputStream {
        return buildFilteredClient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, objectUrl)
                    .pathSegment("download")
                    .build()
            }
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlux<DataBuffer>()
            .toInputStream {
                TransactionTemplate(platformTransactionManager).executeWithoutResult {
                    outboxService.send {
                        DocumentInformatieObjectDownloaded(
                            objectUrl.toString()
                        )
                    }
                }
            }
    }

    fun lockInformatieObject(
        authentication: DocumentenApiAuthentication,
        objectUrl: URI
    ): DocumentLock {
        val result = checkNotNull(
            buildFilteredClient(authentication)
                .post()
                .uri(objectUrl.toString() + "/lock")
                .retrieve()
                .toEntity(DocumentLock::class.java)
                .block()?.body
        ) {
            "Could not lock document at $objectUrl"
        }

        return result
    }

    fun unlockInformatieObject(
        authentication: DocumentenApiAuthentication,
        objectUrl: URI,
        documentLock: DocumentLock,
    ) {
        buildFilteredClient(authentication)
            .post()
            .uri(objectUrl.toString() + "/unlock")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(documentLock))
            .retrieve()
            .toBodilessEntity()
            .block()
    }

    fun deleteInformatieObject(authentication: DocumentenApiAuthentication, url: URI) {
        buildFilteredClient(authentication)
            .delete()
            .uri(url)
            .retrieve()
            .toBodilessEntity()
            .block()

        outboxService.send { DocumentDeleted(url.toASCIIString()) }
    }

    fun modifyInformatieObject(
        authentication: DocumentenApiAuthentication,
        documentUrl: URI,
        patchDocumentRequest: PatchDocumentRequest
    ): DocumentInformatieObject {
        check(getInformatieObject(authentication, documentUrl).status != DocumentStatusType.DEFINITIEF) {
            "InformatieObject ${documentUrl.path.substringAfterLast("/")} with status 'definitief' cannot be updated!"
        }

        val result = checkNotNull(
            buildFilteredClient(authentication)
                .patch()
                .uri(documentUrl)
                .body(BodyInserters.fromValue(patchDocumentRequest))
                .retrieve()
                .toEntity(DocumentInformatieObject::class.java)
                .block()
        ) {
            "Could not retrieve ${DocumentInformatieObject::class.simpleName} at $documentUrl"
        }

        if (result.hasBody()) {
            outboxService.send {
                DocumentUpdated(
                    result.body.url.toASCIIString(),
                    objectMapper.valueToTree(result.body)
                )
            }
        }

        return result.body!!
    }

    private fun toObjectUrl(baseUrl: URI, objectId: String): URI {
        return UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("enkelvoudiginformatieobjecten", objectId)
            .build()
            .toUri()
    }

    private fun buildFilteredClient(authentication: DocumentenApiAuthentication): WebClient {
        return webclientBuilder
            .clone()
            .filter(authentication)
            .filter(ClientTools.zgwErrorHandler())
            .build()
    }

    private fun Flux<DataBuffer>.toInputStream(doOnComplete: Runnable): InputStream {
        val osPipe = PipedOutputStream()
        val isPipe = PipedInputStream(osPipe)
        val flux = this
            .doOnError { isPipe.use {} }
            .doFinally { osPipe.use {} }
            .doOnComplete(doOnComplete)
        DataBufferUtils.write(flux, osPipe).subscribe(DataBufferUtils.releaseConsumer())
        return isPipe
    }
}
