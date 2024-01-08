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

package com.ritense.smartdocuments.client

import com.fasterxml.jackson.core.JsonFactory
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.dto.SmartDocumentsPropertiesDto
import com.ritense.smartdocuments.io.SubInputStream
import com.ritense.smartdocuments.io.UnicodeUnescapeInputStream
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.Base64
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.FilesResponse
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import com.ritense.smartdocuments.domain.SmartDocumentsTemplateData
import com.ritense.smartdocuments.domain.FileStreamResponse


class SmartDocumentsClient(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
    private val smartDocumentsWebClientBuilder: WebClient.Builder,
    private val maxFileSizeMb: Int,
    private val temporaryResourceStorageService: TemporaryResourceStorageService,
) {

    fun getSmartDocumentsTemplateData(smartDocumentsPropertiesDto: SmartDocumentsPropertiesDto): SmartDocumentsTemplateData? {
        return pluginWebClient(smartDocumentsPropertiesDto).get()
            .uri(STRUCTURE_PATH)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { xmlData -> xmlMapper.readValue(xmlData, SmartDocumentsTemplateData::class.java) }
            .doOnError { throw toHttpClientErrorException(it) }
            .block()
    }

    fun generateDocument(
        smartDocumentsRequest: SmartDocumentsRequest,
    ): FilesResponse {
        return webClient().post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON_UTF8)
            .bodyValue(fixRequest(smartDocumentsRequest))
            .retrieve()
            .bodyToMono(FilesResponse::class.java)
            .doOnError { throw toHttpClientErrorException(it) }
            .block()!!
    }

    fun generateDocumentStream(
        smartDocumentsRequest: SmartDocumentsRequest,
        outputFormat: DocumentFormatOption,
    ): FileStreamResponse {
        val response = webClient().post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON_UTF8)
            .bodyValue(fixRequest(smartDocumentsRequest))
            .exchange()
            .block()

        val responseOut = PipedOutputStream()
        val responseIn = PipedInputStream(responseOut)
        val body = response.body(BodyExtractors.toDataBuffers())
            .doOnError {
                responseIn.use {  }
                throw toHttpClientErrorException(it)
            }
            .doFinally { responseOut.use {  } }

        DataBufferUtils.write(body, responseOut).subscribe(DataBufferUtils.releaseConsumer())

        assertHttp200Status(HttpStatus.valueOf(response.statusCode().value()), response, responseIn)

        val responseResourceId = temporaryResourceStorageService.store(responseIn)
        val parsedResponse = temporaryResourceStorageService.getResourceContentAsInputStream(responseResourceId)
            .use { parseSmartDocumentsResponse(it, outputFormat) }

        val resourceIn = temporaryResourceStorageService.getResourceContentAsInputStream(responseResourceId)
        val documentDataIn = toDocumentDataInputStream(resourceIn, parsedResponse)

        return FileStreamResponse(
            parsedResponse.fileName,
            FilenameUtils.getExtension(parsedResponse.fileName),
            documentDataIn
        )
    }

    private fun assertHttp200Status(
        statusCode: HttpStatus,
        response: ClientResponse,
        responseIn: PipedInputStream
    ) {
        if (!statusCode.is2xxSuccessful) {
            throw toHttpClientErrorException(
                WebClientResponseException(
                    statusCode.value(),
                    statusCode.reasonPhrase,
                    response.headers().asHttpHeaders(),
                    responseIn.use {
                        it.readAllBytes()
                    },
                    response.headers().contentType().map { it.charset }.getOrNull()
                )
            )
        }
    }

    private fun fixRequest(smartDocumentsRequest: SmartDocumentsRequest): SmartDocumentsRequest {
        // Bugfix: SmartDocuments throws an error when using an existing templateGroup
        // Note: The templateGroup doesn't have to exist in SmartDocuments for it to generate a document
        return smartDocumentsRequest.copy(
            smartDocument = smartDocumentsRequest.smartDocument.copy(
                selection = smartDocumentsRequest.smartDocument.selection.copy(
                    templateGroup = UUID.randomUUID().toString()
                )
            )
        )
    }

    private fun toHttpClientErrorException(e: Throwable): HttpClientErrorException {
        if (e is WebClientResponseException) {
            val message = when (e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "The request has not been applied because it lacks valid authentication " +
                        "credentials for the target resource. Response received from server:\n" + e.responseBodyAsString

                HttpStatus.BAD_REQUEST -> "The server cannot or will not process the request due to something that is " +
                        "perceived to be a client error (e.g., no valid template specified, user has no privileges for the template," +
                        " malformed request syntax, invalid request message framing, or deceptive request routing)." +
                        " Response received from server:\n" + e.responseBodyAsString

                else -> e.responseBodyAsString
            }
            return HttpClientErrorException(e.statusCode, message)
        } else {
            return HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.message ?: "An unknown error occurred")
        }
    }

    fun setProperties(smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties) {
        this.smartDocumentsConnectorProperties = smartDocumentsConnectorProperties
    }

    private fun pluginWebClient(pluginProperties: SmartDocumentsPropertiesDto): WebClient {
        val basicAuthentication = ExchangeFilterFunctions.basicAuthentication(
            pluginProperties.username,
            pluginProperties.password
        )

        return smartDocumentsWebClientBuilder
            .clone()
            .baseUrl(pluginProperties.url)
            .filter(basicAuthentication)
            .build()
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

        return smartDocumentsWebClientBuilder
            .clone()
            .baseUrl(smartDocumentsConnectorProperties.url!!)
            .filter(basicAuthentication)
            .exchangeStrategies(exchangeStrategies)
            .build()
    }

    private fun parseSmartDocumentsResponse(
        responseInputStream: InputStream,
        outputFormat: DocumentFormatOption
    ): ParsedResponse {
        var fileName: String? = null
        var correctOutputFormat = false
        var documentDataStart = -1L
        var documentDataEnd = -1L

        val jsonParser = JsonFactory().createParser(responseInputStream)
        while (jsonParser.nextToken() != null) {
            val fieldName = jsonParser.currentName
            if ("filename" == fieldName) {
                fileName = jsonParser.nextTextValue()
            } else if ("outputFormat" == fieldName && outputFormat.toString() == jsonParser.nextTextValue()) {
                correctOutputFormat = true
            } else if ("data" == fieldName) {
                jsonParser.nextToken()
                documentDataStart = jsonParser.currentLocation.byteOffset
                jsonParser.finishToken()
                documentDataEnd = jsonParser.currentLocation.byteOffset - 1
            }

            if (correctOutputFormat && fileName != null && documentDataStart != -1L) {
                break
            }
        }
        jsonParser.close()
        if (!correctOutputFormat && fileName == null && documentDataStart == -1L) {
            throw IllegalStateException("SmartDocuments didn't generate any document. Please check the logs above for a HttpClientErrorException.")
        }  else if (!correctOutputFormat) {
            throw IllegalStateException("SmartDocuments failed to generate document with format '$outputFormat'. The requested document format is not present in the output of smart documents.")
        } else if (fileName == null) {
            throw IllegalStateException("SmartDocuments response didn't contain field 'filename'")
        } else if (documentDataStart == -1L) {
            throw IllegalStateException("SmartDocuments failed to generate document")
        }
        return ParsedResponse(fileName, documentDataStart, documentDataEnd)
    }

    private fun toDocumentDataInputStream(jsonIn: InputStream, parsedResponse: ParsedResponse): InputStream {
        val documentDataIn = SubInputStream(jsonIn, parsedResponse.documentDataStart, parsedResponse.documentDataEnd)
        val unescapedIn = UnicodeUnescapeInputStream(documentDataIn)
        return Base64.getDecoder().wrap(unescapedIn)
    }

    private data class ParsedResponse(
        val fileName: String,
        val documentDataStart: Long,
        val documentDataEnd: Long,
    )

    companion object {
        private val xmlMapper = XmlMapper()

        private const val STRUCTURE_PATH = "sdapi/structure"
    }

}
