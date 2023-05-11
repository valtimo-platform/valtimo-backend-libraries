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
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.FileStreamResponse
import com.ritense.smartdocuments.domain.FilesResponse
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import org.apache.commons.io.FilenameUtils
import org.apache.commons.text.StringEscapeUtils
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlux
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.Writer
import java.util.Base64
import java.util.concurrent.Executors

class SmartDocumentsClient(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
    private val smartDocumentsWebClientBuilder: WebClient.Builder,
    private val maxFileSizeMb: Int,
    private val temporaryResourceStorageService: TemporaryResourceStorageService,
) {

    fun generateDocument(
        smartDocumentsRequest: SmartDocumentsRequest,
    ): FilesResponse {
        return webClient().post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON)
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
        val responseOut = PipedOutputStream()
        val responseIn = PipedInputStream(responseOut)

        val bodyFlux = webClient().post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON)
            .bodyValue(fixRequest(smartDocumentsRequest))
            .retrieve()
            .bodyToFlux<DataBuffer>()
            .doOnError {
                responseIn.close()
                throw toHttpClientErrorException(it)
            }
            .doFinally { responseOut.close() }

        DataBufferUtils.write(bodyFlux, responseOut).subscribe(DataBufferUtils.releaseConsumer())
        val responseResourceId = temporaryResourceStorageService.store(responseIn)

        val parsedResponse = temporaryResourceStorageService.getResourceContentAsInputStream(responseResourceId)
            .use { parseSmartDocumentsResponse(it, outputFormat) }

        val resourceIn = temporaryResourceStorageService.getResourceContentAsInputStream(responseResourceId)
        val documentDataIn = toDocumentDataInputStream(resourceIn, parsedResponse)

        return FileStreamResponse(
            parsedResponse.fileName,
            FilenameUtils.getExtension(parsedResponse.fileName),
            Base64.getDecoder().wrap(documentDataIn)
        )
    }

    private fun fixRequest(smartDocumentsRequest: SmartDocumentsRequest): SmartDocumentsRequest {
        return if (smartDocumentsRequest.smartDocument.selection.templateGroup.length > 18) {
            // Bugfix: SmartDocuments always throws error when the templateGroup is longer than 18
            // Note: The templateGroup doesn't have to exist in SmartDocuments for it to generate a document
            smartDocumentsRequest.copy(
                smartDocument = smartDocumentsRequest.smartDocument.copy(
                    selection = smartDocumentsRequest.smartDocument.selection.copy(
                        templateGroup = smartDocumentsRequest.smartDocument.selection.templateGroup.substring(0, 18)
                    )
                )
            )
        } else {
            smartDocumentsRequest
        }
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
                jsonParser.nextToken()
                documentDataEnd = jsonParser.currentLocation.byteOffset - 2
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

    private fun toDocumentDataInputStream(inputStream: InputStream, parsedResponse: ParsedResponse): InputStream {
        val documentDataOut = PipedOutputStream()
        val documentDataIn = PipedInputStream(documentDataOut)
        val documentDataOutWriter = documentDataOut.writer()
        Executors.newSingleThreadExecutor().execute {
            write(documentDataOutWriter, inputStream, parsedResponse.documentDataStart, parsedResponse.documentDataEnd)
            documentDataOutWriter.close()
            inputStream.close()
        }
        return documentDataIn
    }

    private fun write(outputWriter: Writer, inputStream: InputStream, startByteOffset: Long, endByteOffset: Long) {
        inputStream.skipNBytes(startByteOffset)
        var bytePointer = startByteOffset
        while (bytePointer < endByteOffset) {
            val buffer = inputStream.readNBytes((endByteOffset - bytePointer).toInt().coerceAtMost(1024))
            outputWriter.write(StringEscapeUtils.unescapeJson(String(buffer)))
            bytePointer += buffer.size
        }
        outputWriter.flush()
    }

    private data class ParsedResponse(
        val fileName: String,
        val documentDataStart: Long,
        val documentDataEnd: Long,
    )

}
