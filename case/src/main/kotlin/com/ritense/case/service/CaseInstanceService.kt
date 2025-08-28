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

import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.domain.QuickSearch
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.QuickSearchRepository
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.case.web.rest.dto.CaseDefinitionQuickSearchDto
import com.ritense.case_.authorization.CaseDefinitionActionProvider
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.document.domain.Document
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.DocumentSearchService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valueresolver.ValueResolverService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class CaseInstanceService(
    private val caseDefinitionService: CaseDefinitionService,
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val quickSearchRepository: QuickSearchRepository,
    private val documentSearchService: DocumentSearchService,
    private val valueResolverService: ValueResolverService,
    private val authorizationService: AuthorizationService,
) {
    fun search(
        caseDefinitionKey: String,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): Page<CaseListRowDto> {
        // No authorization on this level, as we have to fully rely on the documentSearchService for filtering results
        val caseListColumns = caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(
            caseDefinitionKey
        )
        val newPageable = mutatePageable(caseListColumns, pageable)

        return documentSearchService.search(caseDefinitionKey, searchRequest, newPageable)
            .map { document -> toCaseListRowDto(document, caseListColumns) }
    }

    fun storeQuickSearch(
        caseDefinitionKey: String,
        request: CaseDefinitionQuickSearchDto,
        currentUserId: String,
    ) {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseDefinition::class.java,
                CaseDefinitionActionProvider.VIEW_LIST
            )
        )

        require(
            !quickSearchRepository.existsByCaseDefinitionKeyAndUserIdAndTitle(
                caseDefinitionKey = caseDefinitionKey,
                userId = currentUserId,
                title = request.title
            )
        ) { "Failed to create quick save. A quick save for this user, for this case definition key, " +
            "with this title, already exists."
        }

        quickSearchRepository.save(
            QuickSearch(
                queryPath = request.queryPath,
                title = request.title,
                caseDefinitionKey = caseDefinitionKey,
                userId = currentUserId,
            )
        )
    }

    fun deleteQuickSearch(
        caseDefinitionKey: String,
        currentUserId: String,
        quickSearchTitle: String,
    ) {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseDefinition::class.java,
                CaseDefinitionActionProvider.VIEW_LIST
            )
        )

        require(
            quickSearchRepository.existsByCaseDefinitionKeyAndUserIdAndTitle(
                caseDefinitionKey = caseDefinitionKey,
                userId = currentUserId,
                title = quickSearchTitle
            )
        ) {
            "Failed to delete quick search. A quick search for this user, for this case definition key, " +
            "with this title, already exists."
        }

        quickSearchRepository.deleteByCaseDefinitionKeyAndUserIdAndTitle(caseDefinitionKey, currentUserId, quickSearchTitle)
    }

    fun getQuickSearchList(caseDefinitionKey: String, currentUserId: String): List<QuickSearch> {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseDefinition::class.java,
                CaseDefinitionActionProvider.VIEW_LIST
            )
        )

        return quickSearchRepository.findAllByCaseDefinitionKeyAndUserId(caseDefinitionKey, currentUserId)
    }

    private fun mutatePageable(caseListColumns: Collection<CaseListColumn>, pageable: Pageable): PageRequest {
        val newSortOrders = pageable.sort.map { sortOrder ->
            val caseListColumn = caseListColumns.find { caseListColumn -> caseListColumn.id.key == sortOrder.property }
            val sortingProperty = caseListColumn?.path ?: sortOrder.property
            Sort.Order(sortOrder.direction, sortingProperty, sortOrder.nullHandling)
        }
        val newSort = Sort.by(newSortOrders.toMutableList())
        return PageRequest.of(pageable.pageNumber, pageable.pageSize, newSort)
    }

    private fun toCaseListRowDto(document: Document, caseListColumns: List<CaseListColumn>): CaseListRowDto {
        val paths = caseListColumns.map { it.path }
        val resolvedValuesMap = valueResolverService.resolveValues(document.id().id.toString(), paths)

        val items = caseListColumns.map { caseListColumn ->
            CaseListRowDto.CaseListItemDto(caseListColumn.id.key, resolvedValuesMap[caseListColumn.path])
        }.toMutableList()

        if (items.none { it.key == "assigneeFullName" }) {
            val case = caseDefinitionService.findCaseDefinition(document.definitionId().caseDefinitionId())
            if (case?.canHaveAssignee == true) {
                items.add(CaseListRowDto.CaseListItemDto("assigneeFullName", document.assigneeFullName()))
            }
        }

        return CaseListRowDto(document.id().toString(), items)
    }

}
