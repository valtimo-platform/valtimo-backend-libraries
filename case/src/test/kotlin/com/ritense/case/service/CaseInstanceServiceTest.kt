/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.case.domain.CaseListColumnId
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.domain.EmptyDisplayTypeParameter
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.DocumentSearchService
import com.ritense.valueresolver.ValueResolverService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.UUID
import kotlin.test.assertEquals

class CaseInstanceServiceTest {

    private lateinit var service: CaseInstanceService

    private lateinit var caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository

    private lateinit var documentSearchService: DocumentSearchService

    private lateinit var valueResolverService: ValueResolverService

    @BeforeEach
    fun setUp() {
        caseDefinitionListColumnRepository = mock()
        documentSearchService = mock()
        valueResolverService = mock()
        service = CaseInstanceService(
            caseDefinitionListColumnRepository,
            documentSearchService,
            valueResolverService,
        )

        whenever(DOCUMENT.id()).thenReturn(JsonSchemaDocumentId.newId(UUID.randomUUID()))
        whenever(caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(CASE_DEFINITION_NAME))
            .thenReturn(listOf(FIRST_NAME_CASE_LIST_COLUMN))
        whenever(valueResolverService.resolveValues(DOCUMENT.id().id.toString(), listOf("doc:firstName")))
            .thenReturn(mapOf("doc:firstName" to "John"))
    }

    @Test
    fun `should get case search for case list row`() {
        val searchRequest = SearchWithConfigRequest()
        val pageable = Pageable.ofSize(10)
        whenever(documentSearchService.search(CASE_DEFINITION_NAME, searchRequest, pageable))
            .thenReturn(PageImpl(listOf(DOCUMENT)))

        val documentsPage = service.search(CASE_DEFINITION_NAME, searchRequest, pageable)

        assertEquals(documentsPage.content.size, 1)
        assertEquals(documentsPage.content[0].items.size, 1)
        assertEquals(documentsPage.content[0].items[0].key, "first-name")
        assertEquals(documentsPage.content[0].items[0].value, "John")
    }

    @Test
    fun `should throw error when sorting the search with unknown case list column`() {
        val searchRequest = SearchWithConfigRequest()
        val pageable = PageRequest.of(0,1, Sort.by("unknown-case-list-column"))
        whenever(documentSearchService.search(CASE_DEFINITION_NAME, searchRequest, pageable))
            .thenReturn(PageImpl(listOf(DOCUMENT)))

        assertThrows<InvalidListColumnException> {
            service.search(CASE_DEFINITION_NAME, searchRequest, pageable)
        }
    }

    companion object {
        private const val CASE_DEFINITION_NAME = "my-case-definition-name"
        private val DOCUMENT = mock<JsonSchemaDocument>()
        private val FIRST_NAME_CASE_LIST_COLUMN = CaseListColumn(
            id = CaseListColumnId(CASE_DEFINITION_NAME, "first-name"),
            title = "First name",
            path = "doc:firstName",
            displayType = DisplayType("string", EmptyDisplayTypeParameter()),
            sortable = true,
            defaultSort = ColumnDefaultSort.ASC,
            order = 1
        )
    }
}
