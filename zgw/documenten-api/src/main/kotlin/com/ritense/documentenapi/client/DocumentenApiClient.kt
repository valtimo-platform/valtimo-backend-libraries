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

package com.ritense.documentenapi.client

import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.zgw.ClientTools
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
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
    val webclientBuilder: WebClient.Builder
) {
    fun storeDocument(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
        request: CreateDocumentRequest
    ): CreateDocumentResult {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
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
        return checkNotNull(
            webclientBuilder
                .clone()
                .filter(authentication)
                .build()
                .get()
                .uri(objectUrl)
                .retrieve()
                .toEntity(DocumentInformatieObject::class.java)
                .block()?.body
        ) {
            "Could not retrieve ${DocumentInformatieObject::class.simpleName} at $objectUrl"
        }
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
        return webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, objectUrl)
                    .pathSegment("download")
                    .build()
            }
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve()
            .bodyToFlux<DataBuffer>()
            .toInputStream()
    }

    private fun toObjectUrl(baseUrl: URI, objectId: String): URI {
        return UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("enkelvoudiginformatieobjecten", objectId)
            .build()
            .toUri()
    }

    private fun Flux<DataBuffer>.toInputStream(): InputStream {
        val osPipe = PipedOutputStream()
        val isPipe = PipedInputStream(osPipe)
        val flux = this
            .doOnError { isPipe.use {} }
            .doFinally { osPipe.use {} }
        DataBufferUtils.write(flux, osPipe).subscribe(DataBufferUtils.releaseConsumer())
        return isPipe
    }
}
