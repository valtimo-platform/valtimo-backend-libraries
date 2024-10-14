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

package com.ritense.documentenapi.web.rest

import com.ritense.document.domain.RelatedFile
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.documentenapi.service.DocumentenApiVersionService
import com.ritense.documentenapi.web.rest.dto.ColumnResponse
import com.ritense.documentenapi.web.rest.dto.DocumentenApiVersionDto
import com.ritense.documentenapi.web.rest.dto.ModifyDocumentRequest
import com.ritense.logging.LoggableResource
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URLConnection

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class DocumentenApiResource(
    private val documentenApiService: DocumentenApiService,
    private val documentenApiVersionService: DocumentenApiVersionService
) {
    @GetMapping("/v1/documenten-api/{pluginConfigurationId}/files/{documentId}/download")
    fun downloadDocument(
        @LoggableResource(resourceType = PluginConfiguration::class) @PathVariable(name = "pluginConfigurationId") pluginConfigurationId: String,
        @PathVariable(name = "documentId") documentId: String,
    ): ResponseEntity<InputStreamResource> {

        val documentInputStream = documentenApiService.downloadInformatieObject(pluginConfigurationId, documentId)
        val documentMetadata = documentenApiService.getInformatieObject(pluginConfigurationId, documentId)

        val responseHeaders = HttpHeaders()
        responseHeaders.set("Content-Disposition", "attachment; filename=\"${documentMetadata.bestandsnaam}\"")

        val documentMediaType = try {
            MediaType.valueOf(URLConnection.guessContentTypeFromName(documentMetadata.bestandsnaam))
        } catch (exception: RuntimeException) {
            MediaType.APPLICATION_OCTET_STREAM
        }
        return ResponseEntity
            .ok()
            .headers(responseHeaders)
            .contentType(documentMediaType)
            .body(InputStreamResource(documentInputStream))
    }

    @PutMapping("/v1/documenten-api/{pluginConfigurationId}/files/{documentId}")
    fun modifyDocument(
        @LoggableResource(resourceType = PluginConfiguration::class) @PathVariable(name = "pluginConfigurationId") pluginConfigurationId: String,
        @PathVariable(name = "documentId") documentId: String,
        @RequestBody modifyDocumentRequest: ModifyDocumentRequest,
    ): ResponseEntity<RelatedFile> {
        return ResponseEntity
            .ok()
            .body(documentenApiService.modifyInformatieObject(pluginConfigurationId, documentId, modifyDocumentRequest))
    }

    @DeleteMapping("/v1/documenten-api/{pluginConfigurationId}/files/{documentId}")
    fun deleteDocument(
        @LoggableResource(resourceType = PluginConfiguration::class) @PathVariable(name = "pluginConfigurationId") pluginConfigurationId: String,
        @PathVariable(name = "documentId") documentId: String,
    ): ResponseEntity<Unit> {
        documentenApiService.deleteInformatieObject(pluginConfigurationId, documentId)
        return ResponseEntity
            .noContent()
            .build()
    }

    @GetMapping("/v1/case-definition/{caseDefinitionName}/zgw-document-column")
    fun getColumns(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<ColumnResponse>> {
        val version = documentenApiVersionService.getVersion(caseDefinitionName)
        val columns = documentenApiService.getColumns(caseDefinitionName)
            .map { ColumnResponse.of(it, version) }
        return ResponseEntity.ok(columns)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionName}/documenten-api/version")
    fun getApiVersion(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<DocumentenApiVersionDto> {
        val version = documentenApiVersionService.getVersion(caseDefinitionName)
        return ResponseEntity.ok(DocumentenApiVersionDto.of(version))
    }
}
