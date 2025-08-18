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

package com.ritense.case.service

import com.opencsv.CSVWriter
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.DocumentSearchService
import com.ritense.document.service.JsonSchemaDocumentActionProvider.EXPORT_LIST
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valueresolver.ValueResolverService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter
import java.nio.charset.StandardCharsets

@Transactional
@Service
@SkipComponentScan
class CaseExporter(
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentSearchService: DocumentSearchService,
    private val valueResolverService: ValueResolverService,
    private val userManagementService: UserManagementService,
    private val authorizationService: AuthorizationService
) {
    fun exportCases(
        caseDefinitionKey: String,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): ResponseEntity<ByteArray> {
        val exportableCases = searchExportable(caseDefinitionKey, searchRequest, pageable)

        val writer = StringWriter()
        val csvWriter = CSVWriter(writer)

        val headers = exportableCases
            .flatMap { row -> row.items.map { it.key } }
            .distinct()

        csvWriter.writeNext(headers.toTypedArray())

        exportableCases.forEach { row ->
            val values = headers.map { key ->
                row.items.firstOrNull { it.key == key }?.value?.toString() ?: ""
            }
            csvWriter.writeNext(values.toTypedArray())
        }

        csvWriter.close()

        val responseHeaders = HttpHeaders().apply {
            contentType = MediaType("text", "csv")
            set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${caseDefinitionKey}_cases.csv\"")
        }

        return ResponseEntity
            .ok()
            .headers(responseHeaders)
            .body(writer.toString().toByteArray(StandardCharsets.UTF_8))
    }

    fun searchExportable(
        caseDefinitionKey: String,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): List<CaseListRowDto> {
        val exportableColumns = getExportableColumns(caseDefinitionKey)

        val newPageable = mutatePageable(exportableColumns, pageable)

        val searchResults = documentSearchService.search(
            caseDefinitionKey,
            searchRequest,
            newPageable
        )

        checkResultsFound(searchResults, caseDefinitionKey)
        requireExportLimit(searchResults, caseDefinitionKey)

        val documents = searchResults.content.filterIsInstance<JsonSchemaDocument>()

        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                JsonSchemaDocument::class.java,
                EXPORT_LIST,
                documents
            )
        )

        val exportableCases = searchResults.content.map { toCaseListRowDto(it, exportableColumns) }
        logExport(caseDefinitionKey, exportableColumns, exportableCases.size.toLong())

        return exportableCases
    }

    private fun getExportableColumns(caseDefinitionKey: String): List<CaseListColumn> {
        val exportableColumns = caseDefinitionListColumnRepository
            .findByIdCaseDefinitionKeyOrderByOrderAsc(caseDefinitionKey)
            .filter { it.exportable }

        require(exportableColumns.isNotEmpty()) {
            logger.warn {
                "User '${currentUserInfo()}' " +
                    "attempted export for case '$caseDefinitionKey' but no exportable columns were found."
            }
            "Export failed: no exportable columns found."
        }
        return exportableColumns
    }

    private fun mutatePageable(
        caseListColumns: Collection<CaseListColumn>,
        pageable: Pageable
    ): PageRequest {
        val keyToPath = caseListColumns.associate { it.id.key to it.path }
        val orders = pageable.sort.map { sortOrder ->
            val sortingProperty = keyToPath[sortOrder.property] ?: sortOrder.property
            Sort.Order(sortOrder.direction, sortingProperty, sortOrder.nullHandling)
        }

        val newSort = if (orders.isEmpty) Sort.unsorted() else Sort.by(orders.toMutableList())

        return PageRequest.of(PAGE_FIRST, MAX_EXPORT, newSort)
    }

    private fun checkResultsFound(results: Page<*>, caseDefinitionKey: String) {
        check(!results.isEmpty) {
            logger.info {
                "User '${currentUserInfo()}' " +
                    "attempted export for case '$caseDefinitionKey' but the search returned no results."
            }
            "Export failed: search returned no results."
        }
    }

    private fun logExport(caseDefinitionKey: String, columns: List<CaseListColumn>, total: Long) {
        logger.info {
            "User '${currentUserInfo()}' exported $total cases for '$caseDefinitionKey'. " +
                "Exported columns: [${columns.joinToString(", ") { it.id.key }}]"
        }
    }

    private fun requireExportLimit(results: Page<*>, caseDefinitionKey: String) {
        require(results.totalElements <= MAX_EXPORT) {
            "Export failed for case '$caseDefinitionKey': the number of cases exceeds the maximum limit of 10,000. Please refine your search criteria."
        }
    }

    private fun toCaseListRowDto(document: Document, caseListColumns: List<CaseListColumn>): CaseListRowDto {
        val paths = caseListColumns.map { it.path }
        val resolvedValuesMap = valueResolverService.resolveValues(document.id().id.toString(), paths)

        val items = caseListColumns.map { caseListColumn ->
            CaseListRowDto.CaseListItemDto(caseListColumn.id.key, resolvedValuesMap[caseListColumn.path])
        }.toMutableList()

        return CaseListRowDto(document.id().toString(), items)
    }

    private fun currentUserInfo(): String =
        userManagementService.currentUser.let { "${it.fullName} (${it.email})" }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_EXPORT = 10_000
        private const val PAGE_FIRST = 0
    }
}
