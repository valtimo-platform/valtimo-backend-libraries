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

import com.ritense.case.domain.CaseListColumn
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.Document
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.DocumentSearchService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valueresolver.ValueResolverService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class CaseExporter(
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentSearchService: DocumentSearchService,
    private val valueResolverService: ValueResolverService,
    private val userManagementService: UserManagementService
) {
    fun searchExportable(
        caseDefinitionKey: String,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): Page<CaseListRowDto> {
        val exportableColumns = getExportableColumns(caseDefinitionKey)

        val searchResults = documentSearchService.search(
            caseDefinitionKey,
            searchRequest,
            mutatePageable(exportableColumns, pageable)
        )

        checkResultsFound(searchResults, caseDefinitionKey)
        requireExportLimit(searchResults, caseDefinitionKey)

        val exportableCases = searchResults.map { toCaseListRowDto(it, exportableColumns) }
        logExport(caseDefinitionKey, exportableColumns, exportableCases.totalElements)

        return exportableCases
    }

    private fun getExportableColumns(caseDefinitionKey: String): List<CaseListColumn> {
        val exportableColumns = caseDefinitionListColumnRepository
            .findByIdCaseDefinitionKeyOrderByOrderAsc(caseDefinitionKey)
            .filter { it.exportable }

        require(exportableColumns.isNotEmpty()) {
            logger.warn {
                "User '${getCurrentUser().fullName} (${getCurrentUser().email})' " +
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
        val size = pageable.pageSize.coerceAtMost(MAX_PAGE_SIZE)

        if (pageable.sort.isUnsorted) {
            return PageRequest.of(pageable.pageNumber, size, pageable.sort)
        }

        val keyToPath = caseListColumns.associate { it.id.key to it.path }
        val newSortOrders = pageable.sort.map { sortOrder ->
            val sortProperty = keyToPath[sortOrder.property] ?: sortOrder.property
            Sort.Order(sortOrder.direction, sortProperty, sortOrder.nullHandling)
        }
        val newSort = Sort.by(newSortOrders.toMutableList())
        return PageRequest.of(pageable.pageNumber, pageable.pageSize, newSort)
    }

    private fun checkResultsFound(results: Page<*>, caseDefinitionKey: String) {
        check(!results.isEmpty) {
            logger.info {
                "User '${getCurrentUser().fullName} (${getCurrentUser().email})' " +
                    "attempted export for case '$caseDefinitionKey' but the search returned no results."
            }
            "Export failed: search returned no results."
        }
    }

    private fun logExport(caseDefinitionKey: String, columns: List<CaseListColumn>, total: Long) {
        logger.info {
            "User '${getCurrentUser().fullName} (${getCurrentUser().email})' exported $total cases for '$caseDefinitionKey'. " +
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

    private fun getCurrentUser(): ManageableUser {
        return userManagementService.currentUser
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_EXPORT = 10_000
        private const val MAX_PAGE_SIZE = 50
    }
}
