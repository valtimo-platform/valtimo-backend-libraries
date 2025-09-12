/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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
import com.ritense.case.domain.CaseExportRequest
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.service.exception.ExportLimitExceedsException
import com.ritense.case.service.exception.NoExportableColumnsException
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.event.DocumentsExported
import com.ritense.document.service.impl.JsonSchemaDocumentSearchService
import com.ritense.outbox.OutboxService
import com.ritense.valtimo.contract.utils.SecurityUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.function.Supplier
import kotlin.text.Charsets.UTF_8

@Transactional
class CaseExporter(
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentSearchService: JsonSchemaDocumentSearchService,
    private val outboxService: OutboxService,
    private val mapper: ObjectMapper,
    private val caseListRowMapper: CaseListRowMapper
) {
    fun exportCases(
        caseDefinitionKey: String,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): ResponseEntity<ByteArray> {
        val userLabel = currentUserInfo()

        val exportableColumns = getExportableColumns(caseDefinitionKey, userLabel)

        val exportableCases = searchExportable(caseDefinitionKey, exportableColumns, searchRequest, pageable)

        logExport(
            caseDefinitionKey,
            exportableColumns,
            exportableCases.size.toLong(),
            userLabel,
            searchRequest
        )

        val exportRequest = CaseExportRequest(caseDefinitionKey, searchRequest)

        outboxService.send(Supplier {
            DocumentsExported(
                mapper.valueToTree(exportRequest)
            )
        })

        return toCsvResponse(caseDefinitionKey, exportableCases, exportableColumns)
    }

    private fun toCsvResponse(
        caseDefinitionKey: String,
        exportableCases: List<CaseListRowDto>,
        exportableColumns: List<CaseListColumn>
    ): ResponseEntity<ByteArray> {
        val headers: List<String> = exportableColumns.map { it.id.key }.distinct()

        val csvText = StringWriter().use { writer ->
            CSVWriter(writer).use { csv ->
                csv.writeNext(headers.toTypedArray(), false)
                exportableCases.forEach { row ->
                    val map = row.items.associate { it.key to (it.value?.toString() ?: "") }
                    val values = headers.map { header -> map[header] ?: "" }.toTypedArray()
                    csv.writeNext(values, false)
                }
            }
            writer.toString()
        }

        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val responseHeaders = HttpHeaders().apply {
            contentType = MediaType("text", "csv")
            set(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${caseDefinitionKey}_cases_export_${currentDate}.csv\""
            )
        }

        return ResponseEntity.ok()
            .headers(responseHeaders)
            .body(csvText.toByteArray(UTF_8))
    }

    private fun searchExportable(
        caseDefinitionKey: String,
        exportableColumns: List<CaseListColumn>,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): List<CaseListRowDto> {
        val userLabel = currentUserInfo()

        val newPageable = mutatePageable(exportableColumns, pageable)

        val searchResults = documentSearchService.searchForExport(
            caseDefinitionKey,
            searchRequest,
            newPageable
        )

        validateExportLimit(searchResults, caseDefinitionKey)

        val exportableCases = searchResults
            .content
            .map { caseListRowMapper.toCaseListRowDto(it as JsonSchemaDocument, exportableColumns) }

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
            val filterStrings = filters.map { filter ->
                "${filter.key} = ${filter.getValues<Any>().joinToString(",")}"
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

    private fun currentUserInfo(): String =
        SecurityUtils.getCurrentUserLogin() ?: "system"

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_EXPORT = 10_000
        private const val PAGE_FIRST = 0
    }
}
