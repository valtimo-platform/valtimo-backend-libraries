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

package com.ritense.smartdocuments.client

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.FileStreamResponse
import com.ritense.smartdocuments.domain.FilesResponse
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import com.ritense.smartdocuments.domain.SmartDocumentsTemplateData
import com.ritense.smartdocuments.dto.SmartDocumentsPropertiesDto
import com.ritense.smartdocuments.io.SubInputStream
import com.ritense.smartdocuments.io.UnicodeUnescapeInputStream
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8
import com.ritense.valtimo.web.logging.RestClientLoggingExtension
import org.apache.commons.io.FilenameUtils
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.Base64
import java.util.UUID

class SmartDocumentsClient(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties, // WHY CONNECTOR? is this class old?/depracted
    private val smartDocumentsRestClientBuilder: RestClient.Builder,
    private val maxFileSizeMb: Int,
    private val temporaryResourceStorageService: TemporaryResourceStorageService,
) {

    fun getSmartDocumentsTemplateData(smartDocumentsPropertiesDto: SmartDocumentsPropertiesDto): SmartDocumentsTemplateData? {
        val response = pluginRestClient(smartDocumentsPropertiesDto)
            .get()
            .uri(STRUCTURE_PATH)
            .retrieve()
            .body(String::class.java)!!
        return xmlMapper.readValue(response, SmartDocumentsTemplateData::class.java)
    }

    fun generateDocument(
        smartDocumentsRequest: SmartDocumentsRequest,
    ): FilesResponse {
        return restClient().post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON_UTF8)
            .body(fixRequest(smartDocumentsRequest))
            .retrieve()
            .body(FilesResponse::class.java)!!
    }

    fun generateDocumentStream(
        smartDocumentsRequest: SmartDocumentsRequest,
        outputFormat: DocumentFormatOption,
    ): FileStreamResponse {
        val outputStream = PipedOutputStream()
        restClient()
            .post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON_UTF8)
            .body(fixRequest(smartDocumentsRequest))
            .exchange { _, response ->
                if (response.statusCode.equals(HttpStatus.OK)) {
                    response.body.copyTo(outputStream)
                } else {
                    throw toHttpClientErrorException(
                        WebClientResponseException(
                            response.statusCode.value(),
                            response.statusText,
                            response.headers,
                            response.body.readAllBytes(),
                            response.headers.contentType.charset
                        )
                    )
                }
            }

        val responseIn = PipedInputStream(outputStream)
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

    private fun pluginRestClient(pluginProperties: SmartDocumentsPropertiesDto): RestClient {
        return smartDocumentsRestClientBuilder
            .clone()
            .baseUrl(pluginProperties.url)
            .defaultHeaders { headers ->
                headers.setBasicAuth(pluginProperties.username, pluginProperties.password)
            }
            .apply { RestClientLoggingExtension.defaultRequestLogging(it) }
            .build()
    }

    private fun restClient(): RestClient {
        // Setting the max file size for the smart documents response
        val exchangeStrategies = ExchangeStrategies
            .builder()
            .codecs { configurer: ClientCodecConfigurer ->
                configurer.defaultCodecs().maxInMemorySize(1024 * 1024 * maxFileSizeMb)
            }
            .build()

        return smartDocumentsRestClientBuilder
            .clone()
            .baseUrl(smartDocumentsConnectorProperties.url!!)
            .defaultHeaders { headers ->
                headers.setBasicAuth(
                    smartDocumentsConnectorProperties.username!!,
                    smartDocumentsConnectorProperties.password!!
                )
            }
            // .exchangeStrategies(exchangeStrategies) TODO fix for RestClient
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
        } else if (!correctOutputFormat) {
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

        private const val STRUCTURE_PATH = "/sdapi/structure"
    }

}
