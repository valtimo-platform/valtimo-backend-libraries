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

import com.fasterxml.jackson.databind.ObjectMapper
import com.opencsv.CSVWriter
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.CaseExportRequest
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.service.exception.ExportLimitExceedsException
import com.ritense.case.service.exception.NoExportPermissionException
import com.ritense.case.service.exception.NoExportableColumnsException
import com.ritense.case.service.exception.NoSearchResultsException
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.event.DocumentsExported
import com.ritense.document.service.DocumentSearchService
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.Companion.EXPORT
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.outbox.OutboxService
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
import java.util.function.Supplier

@Transactional
@Service
@SkipComponentScan
class CaseExporter(
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentSearchService: DocumentSearchService,
    private val valueResolverService: ValueResolverService,
    private val userManagementService: UserManagementService,
    private val authorizationService: AuthorizationService,
    private val jsonSchemaDocumentDefinitionService: JsonSchemaDocumentDefinitionService,
    private val outboxService: OutboxService,
    private val mapper: ObjectMapper
) {
    fun exportCases(
        caseDefinitionKey: String,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): ResponseEntity<ByteArray> {
        val exportableCases = searchExportable(caseDefinitionKey, searchRequest, pageable)

        val exportRequest = CaseExportRequest(caseDefinitionKey, searchRequest)

        outboxService.send(Supplier {
            DocumentsExported(
                mapper.valueToTree(exportRequest)
            )
        })

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
        val userLabel = currentUserInfo()

        val exportableColumns = getExportableColumns(caseDefinitionKey, userLabel)

        val newPageable = mutatePageable(exportableColumns, pageable)

        val searchResults = documentSearchService.search(
            caseDefinitionKey,
            searchRequest,
            newPageable
        )

        validateResultsFound(searchResults, caseDefinitionKey, userLabel)
        validateExportLimit(searchResults, caseDefinitionKey)
        validateExportPermission(caseDefinitionKey, userLabel)

        val exportableCases = searchResults.content.map { toCaseListRowDto(it, exportableColumns) }

        logExport(caseDefinitionKey, exportableColumns, exportableCases.size.toLong(), userLabel, searchRequest)

        return exportableCases
    }

    private fun getExportableColumns(caseDefinitionKey: String, currentUser: String): List<CaseListColumn> {
        val exportableColumns = caseDefinitionListColumnRepository
            .findByIdCaseDefinitionKeyOrderByOrderAsc(caseDefinitionKey)
            .filter { it.exportable }

        if (exportableColumns.isEmpty()) {
            logger.warn {
                "User '$currentUser' attempted export for case '$caseDefinitionKey' but no exportable columns were found."
            }
            throw NoExportableColumnsException()
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

    private fun validateResultsFound(results: Page<*>, caseDefinitionKey: String, currentUser: String) {
        if (results.isEmpty) {
            logger.info {
                "User '$currentUser' attempted export for case '$caseDefinitionKey' but the search returned no results."
            }
            throw NoSearchResultsException()
        }
    }

    private fun logExport(
        caseDefinitionKey: String,
        columns: List<CaseListColumn>,
        total: Long,
        currentUser: String,
        searchRequest: SearchWithConfigRequest
    ) {
        val logs = mutableListOf<String>()

        logs += "User '$currentUser' exported $total case(s) for '$caseDefinitionKey'."
        logs += "Exported columns: [${columns.joinToString(", ") { it.id.key }}]."

        searchRequest.statusFilter?.takeIf { it.isNotEmpty() }?.let { statuses ->
            logs += "Status filter: [${statuses.joinToString(", ")}]."
        }

        searchRequest.otherFilters?.takeIf { it.isNotEmpty() }?.let { filters ->
            val filterStrings = filters.map { f ->
                "${f.key} = ${f.getValues<Any>().joinToString(",")}"
            }
            logs += "Other filters: ${filterStrings.joinToString("; ")}."
        }

        logger.info { logs.joinToString(" ") }
    }

    private fun validateExportLimit(results: Page<*>, caseDefinitionKey: String) {
        if (results.totalElements > MAX_EXPORT) {
            throw ExportLimitExceedsException(caseDefinitionKey)
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

    private fun validateExportPermission(caseDefinitionKey: String, currentUser: String) {

        val documentDefinition = jsonSchemaDocumentDefinitionService.findActiveByName(caseDefinitionKey)

        val hasExportPermission = authorizationService.hasPermission(
            EntityAuthorizationRequest(
                JsonSchemaDocumentDefinition::class.java,
                EXPORT,
                documentDefinition.get()
            )
        )

        if (!hasExportPermission) {
            logger.warn { "User '$currentUser' has no permission to  export case '$caseDefinitionKey'." }
            throw NoExportPermissionException(caseDefinitionKey)
        }
    }

    private fun currentUserInfo(): String =
        userManagementService.currentUser.let { "${it.fullName} (${it.email})" }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_EXPORT = 10_000
        private const val PAGE_FIRST = 0
    }
}
