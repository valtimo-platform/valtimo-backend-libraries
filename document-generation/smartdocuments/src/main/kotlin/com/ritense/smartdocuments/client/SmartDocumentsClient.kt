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
import org.apache.commons.io.FilenameUtils
import org.apache.commons.text.StringEscapeUtils
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlux
import java.io.File
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.RandomAccessFile
import java.io.Writer
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.Executors
import kotlin.io.path.deleteIfExists


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
            val bodyFlux = webClient().post()
                .uri("/wsxmldeposit/deposit/unattended")
                .contentType(APPLICATION_JSON)
                .bodyValue(smartDocumentsRequest)
                .retrieve()
                .bodyToFlux<DataBuffer>()

            val tempFile = Files.createTempFile("smartDocumentsResponse", ".tmp")
            DataBufferUtils.write(bodyFlux, tempFile).share().block()

            val parsedResponse = parseSmartDocumentsResponseFile(tempFile.toFile(), outputFormat)
            val dataInputStream = fileDataToInputStream(
                file = tempFile.toFile(),
                parsedResponse = parsedResponse,
                onComplete = { tempFile.deleteIfExists() }
            )

            return FileStreamResponse(
                parsedResponse.fileName,
                FilenameUtils.getExtension(parsedResponse.fileName),
                dataInputStream
            )
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

        return smartDocumentsWebClientBuilder
            .clone()
            .baseUrl(smartDocumentsConnectorProperties.url!!)
            .filter(basicAuthentication)
            .exchangeStrategies(exchangeStrategies)
            .build()
    }

    private fun parseSmartDocumentsResponseFile(
        responseFile: File,
        outputFormat: DocumentFormatOption
    ): ParsedResponse {
        var fileName: String? = null
        var correctOutputFormat = false
        var documentDataStart = -1L
        var documentDataEnd = -1L

        val jsonParser = JsonFactory().createParser(responseFile)
        while (jsonParser.nextToken() !== JsonToken.END_ARRAY) {
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
        if (!correctOutputFormat) {
            throw IllegalStateException("SmartDocuments didn't generate document with format '$outputFormat'")
        }
        if (fileName == null) {
            throw IllegalStateException("SmartDocuments response didn't contain field 'filename'")
        }
        if (documentDataStart == -1L) {
            throw IllegalStateException("SmartDocuments didn't generate document")
        }
        return ParsedResponse(fileName, documentDataStart, documentDataEnd)
    }

    private fun fileDataToInputStream(file: File, parsedResponse: ParsedResponse, onComplete: () -> Unit): InputStream {
        val documentDataOut = PipedOutputStream()
        val documentDataIn = PipedInputStream(documentDataOut)
        val documentDataOutWriter = documentDataOut.writer()

        Executors.newSingleThreadExecutor().execute {
            writeFileData(documentDataOutWriter, file, parsedResponse.documentDataStart, parsedResponse.documentDataEnd)
            documentDataOutWriter.close()
            onComplete.invoke()
        }
        return Base64.getDecoder().wrap(documentDataIn)
    }

    private fun writeFileData(outputWriter: Writer, inputFile: File, startByteOffset: Long, endByteOffset: Long) {
        val raf = RandomAccessFile(inputFile, "r")
        raf.seek(startByteOffset)
        val buffer = ByteArray(1024)
        while (raf.filePointer < endByteOffset) {
            val len = raf.read(buffer, 0, (endByteOffset - raf.filePointer).toInt().coerceAtMost(buffer.size))
            outputWriter.write(StringEscapeUtils.unescapeJson(String(buffer, 0, len)))
        }
        outputWriter.flush()
    }

    private data class ParsedResponse(
        val fileName: String,
        val documentDataStart: Long,
        val documentDataEnd: Long,
    )

}
