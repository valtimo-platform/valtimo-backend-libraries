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

package com.ritense.case.service

import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.DocumentSearchService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.document.domain.Document
import com.ritense.valueresolver.ValueResolverService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.zalando.problem.Status

@Transactional
class CaseInstanceService(
    private val caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
    private val documentSearchService: DocumentSearchService,
    private val valueResolverService: ValueResolverService,
) {
    fun search(
        caseDefinitionName: String,
        searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): Page<CaseListRowDto> {

        val caseListColumns = caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
            caseDefinitionName
        )
        val newPageable = mutatePageable(caseListColumns, pageable)

        return documentSearchService.search(caseDefinitionName, searchRequest, newPageable)
            .map { document -> toCaseListRowDto(document, caseListColumns) }
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

        return CaseListRowDto(document.id().toString(), caseListColumns.map { caseListColumn ->
            CaseListRowDto.CaseListItemDto(caseListColumn.id.key, resolvedValuesMap[caseListColumn.path])
        })
    }

}
