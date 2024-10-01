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

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.documentenapi.service.DocumentenApiVersionService
import com.ritense.documentenapi.web.rest.dto.ColumnKeyResponse
import com.ritense.documentenapi.web.rest.dto.ColumnResponse
import com.ritense.documentenapi.web.rest.dto.DocumentenApiVersionManagementDto
import com.ritense.documentenapi.web.rest.dto.DocumentenApiVersionsManagementDto
import com.ritense.documentenapi.web.rest.dto.ReorderColumnRequest
import com.ritense.documentenapi.web.rest.dto.UpdateColumnRequest
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class DocumentenApiManagementResource(
    private val documentenApiService: DocumentenApiService,
    private val documentenApiVersionService: DocumentenApiVersionService
) {
    @RunWithoutAuthorization
    @GetMapping("/v1/case-definition/{caseDefinitionName}/zgw-document-column-key")
    fun getColumnKeys(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<ColumnKeyResponse>> {
        val version = documentenApiVersionService.getVersion(caseDefinitionName)
        val columns = documentenApiService.getAllColumnKeys(caseDefinitionName)
            .map { ColumnKeyResponse.of(it, version) }
        return ResponseEntity.ok(columns)
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/case-definition/{caseDefinitionName}/zgw-document-column")
    fun getConfiguredColumns(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<ColumnResponse>> {
        val version = documentenApiVersionService.getVersion(caseDefinitionName)
        val columns = documentenApiService.getColumns(caseDefinitionName)
            .map { ColumnResponse.of(it, version) }
        return ResponseEntity.ok(columns)
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/case-definition/{caseDefinitionName}/zgw-document-column")
    fun updateColumnOrder(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @RequestBody columnDtos: List<ReorderColumnRequest>,
    ): ResponseEntity<List<ColumnResponse>> {
        val version = documentenApiVersionService.getVersion(caseDefinitionName)
        val columns = columnDtos.mapIndexed { index, columnDto -> columnDto.toEntity(caseDefinitionName, index) }
        val updatedColumns = documentenApiService.updateColumnOrder(columns)
            .map { ColumnResponse.of(it, version) }
        return ResponseEntity.ok(updatedColumns)
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/case-definition/{caseDefinitionName}/zgw-document-column/{columnKey}")
    fun createOrUpdateColumn(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @PathVariable(name = "columnKey") columnKey: String,
        @RequestBody column: UpdateColumnRequest,
    ): ResponseEntity<ColumnResponse> {
        val version = documentenApiVersionService.getVersion(caseDefinitionName)
        val updatedColumn = documentenApiService.createOrUpdateColumn(column.toEntity(caseDefinitionName, columnKey))
        return ResponseEntity.ok(ColumnResponse.of(updatedColumn, version))
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/case-definition/{caseDefinitionName}/zgw-document-column/{columnKey}")
    fun deleteColumn(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @PathVariable(name = "columnKey") columnKey: String,
    ): ResponseEntity<Unit> {
        documentenApiService.deleteColumn(caseDefinitionName, columnKey)
        return ResponseEntity.ok().build()
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/case-definition/{caseDefinitionName}/documenten-api/version")
    fun getApiVersion(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<DocumentenApiVersionManagementDto> {
        val apiVersions = documentenApiVersionService.detectPluginVersions(caseDefinitionName)
            .mapNotNull { it.third }
        return ResponseEntity.ok(DocumentenApiVersionManagementDto.of(apiVersions))
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/documenten-api/versions")
    fun getAllApiVersion(): ResponseEntity<DocumentenApiVersionsManagementDto> {
        val versions = documentenApiVersionService.getAllVersions().map { it.version }
        return ResponseEntity.ok(DocumentenApiVersionsManagementDto(versions))
    }
}
