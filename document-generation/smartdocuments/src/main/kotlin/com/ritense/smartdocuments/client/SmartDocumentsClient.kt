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

package com.ritense.smartdocuments.client

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.FileStreamResponse
import com.ritense.smartdocuments.domain.FilesResponse
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import org.springframework.http.HttpStatus
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.aspectj.util.FileUtil.Pipe
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.OutputStreamWriter
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.time.Duration
import reactor.netty.http.client.HttpClient


class SmartDocumentsClient(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
    private val smartDocumentsWebClientBuilder: WebClient.Builder,
    private val maxFileSizeMb: Int
) {

    fun generateDocument(
        smartDocumentsRequest: SmartDocumentsRequest,
    ): FilesResponse {
        try {
            return webClient().post()
                .uri("/wsxmldeposit/deposit/unattended")
                .contentType(APPLICATION_JSON)
                .bodyValue(smartDocumentsRequest)
                .retrieve()
                .bodyToMono(FilesResponse::class.java)
                .block()!!
        } catch (e: WebClientResponseException) {
            throw HttpClientErrorException(e.statusCode, e.responseBodyAsString)
        }
    }

    fun generateDocumentStream(
        smartDocumentsRequest: SmartDocumentsRequest,
        outputFormat: DocumentFormatOption,
    ): FileStreamResponse {
        try {
            val responseOut = PipedOutputStream()
            val responseIn = PipedInputStream(responseOut)

            webClient().post()
                .uri("/wsxmldeposit/deposit/unattended")
                .contentType(APPLICATION_JSON)
                .bodyValue(smartDocumentsRequest)
                .retrieve()
                .toEntityFlux()
                .exchangeToFlux { response ->
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        response.body(BodyExtractors.toDataBuffers())
                    } else {
                        response.createException().flatMapMany { Mono.error(it) }
                    }
                }
                .publishOn(Schedulers.boundedElastic())
                .map { bodyPart -> responseOut.write(bodyPart.asInputStream().readBytes()) }
                .blockLast(Duration.ofMinutes(5))

            var fileName: String? = null
            var correctOutputFormat = false
            var writtenDocumentData = false

            val documentDataOut = PipedOutputStream()
            val documentDataIn = PipedInputStream(documentDataOut)
            val documentDataWriter = OutputStreamWriter(documentDataOut)

            val jsonParser = JsonFactory().createParser(responseIn)
            while (jsonParser.nextToken() !== JsonToken.END_OBJECT) {
                val fieldName = jsonParser.currentName
                if ("filename" == fieldName) {
                    fileName = jsonParser.nextTextValue()
                } else if ("outputFormat" == fieldName && outputFormat.toString() == jsonParser.nextTextValue()) {
                    correctOutputFormat = true
                } else if ("data" == fieldName) {
                    jsonParser.nextToken()
                    jsonParser.getText(documentDataWriter)
                    writtenDocumentData = true
                }

                if (correctOutputFormat && fileName != null && writtenDocumentData) {
                    break
                }
            }
            jsonParser.close()

            return FileStreamResponse(fileName!!, documentDataIn)
        } catch (e: WebClientResponseException) {
            throw HttpClientErrorException(e.statusCode, e.responseBodyAsString)
        }
    }

    fun setProperties(smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties) {
        this.smartDocumentsConnectorProperties = smartDocumentsConnectorProperties
    }

    private fun webClient(): WebClient {
        val basicAuthentication = ExchangeFilterFunctions.basicAuthentication(
            smartDocumentsConnectorProperties.username!!,
            smartDocumentsConnectorProperties.password!!
        )

        // Setting the max file size for the smart documents response
        val exchangeStrategies = ExchangeStrategies
            .builder()
            .codecs { configurer: ClientCodecConfigurer ->
                configurer.defaultCodecs().maxInMemorySize(1024 * 1024 * maxFileSizeMb)
            }
            .build()

        val sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()

        val httpClient = HttpClient.create().secure { t -> t.sslContext(sslContext) }

        return smartDocumentsWebClientBuilder
            .clone()
            .baseUrl(smartDocumentsConnectorProperties.url!!)
            .filter(basicAuthentication)
            .exchangeStrategies(exchangeStrategies)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

}
