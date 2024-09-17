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
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.Resource
import org.springframework.http.converter.ResourceHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.io.InputStream
import java.util.Base64
import java.util.UUID

class SmartDocumentsClient(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
    private val smartDocumentsRestClientBuilder: RestClient.Builder,
    private val maxFileSizeMb: Int,
    private val temporaryResourceStorageService: TemporaryResourceStorageService,
) {

    fun getSmartDocumentsTemplateData(smartDocumentsPropertiesDto: SmartDocumentsPropertiesDto): SmartDocumentsTemplateData? {
        val response = pluginRestClient(smartDocumentsPropertiesDto)
            .get()
            .uri(STRUCTURE_PATH)
            .retrieve()
            .body<String>()!!
        return xmlMapper.readValue(response, SmartDocumentsTemplateData::class.java)
    }

    fun generateDocument(smartDocumentsRequest: SmartDocumentsRequest): FilesResponse {
        return restClient()
            .post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON_UTF8)
            .body(fixRequest(smartDocumentsRequest))
            .retrieve()
            .body<FilesResponse>()!!
    }

    fun generateDocumentStream(
        smartDocumentsRequest: SmartDocumentsRequest,
        outputFormat: DocumentFormatOption,
    ): FileStreamResponse {
        // Stream complete response (json) to a Resource
        val result = restClient()
            .post()
            .uri("/wsxmldeposit/deposit/unattended")
            .contentType(APPLICATION_JSON_UTF8)
            .body(fixRequest(smartDocumentsRequest))
            .retrieve()
            .body<Resource>()!!

        val responseResourceId = temporaryResourceStorageService.store(result.inputStream)
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

    fun setProperties(smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties) {
        this.smartDocumentsConnectorProperties = smartDocumentsConnectorProperties
    }

    private fun pluginRestClient(pluginProperties: SmartDocumentsPropertiesDto): RestClient {
        return smartDocumentsRestClientBuilder
            .clone()
            .baseUrl(pluginProperties.url)
            .defaultHeaders { headers ->
                headers.setBasicAuth(
                    pluginProperties.username,
                    pluginProperties.password
                )
            }
            .build()
    }

    private fun restClient(): RestClient {
        return smartDocumentsRestClientBuilder
            .clone()
            .baseUrl(smartDocumentsConnectorProperties.url!!)
            .defaultHeaders { headers ->
                headers.setBasicAuth(
                    smartDocumentsConnectorProperties.username!!,
                    smartDocumentsConnectorProperties.password!!
                )
            }
            .messageConverters {
                it + ResourceHttpMessageConverter(true) // Enables streaming
            }
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
