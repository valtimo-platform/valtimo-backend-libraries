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

import com.ritense.BaseTest
import com.ritense.authorization.AuthorizationService
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.domain.CaseListColumnId
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.QuickSearchRepository
import com.ritense.case.web.rest.dto.CaseDefinitionQuickSearchDto
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.DocumentSearchService
import com.ritense.search.domain.DisplayType
import com.ritense.search.domain.EmptyDisplayTypeParameter
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.ValueResolverService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.lang.IllegalArgumentException
import java.util.UUID
import kotlin.test.assertEquals

class CaseInstanceServiceTest : BaseTest() {

    private lateinit var service: CaseInstanceService

    private lateinit var caseDefinitionService: CaseDefinitionService

    private lateinit var caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository

    private lateinit var documentSearchService: DocumentSearchService

    private lateinit var valueResolverService: ValueResolverService

    private lateinit var quickSearchRepository: QuickSearchRepository

    private lateinit var authorizationService: AuthorizationService

    @BeforeEach
    fun setUp() {
        caseDefinitionService = mock()
        caseDefinitionListColumnRepository = mock()
        documentSearchService = mock()
        valueResolverService = mock()
        quickSearchRepository = mock()
        authorizationService = mock()
        service = CaseInstanceService(
            caseDefinitionService,
            caseDefinitionListColumnRepository,
            quickSearchRepository,
            documentSearchService,
            valueResolverService,
            authorizationService,
        )

        whenever(DOCUMENT.id()).thenReturn(JsonSchemaDocumentId.newId(UUID.randomUUID()))
        whenever(caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(CASE_DEFINITION_NAME))
            .thenReturn(listOf(FIRST_NAME_CASE_LIST_COLUMN))
        whenever(valueResolverService.resolveValues(DOCUMENT.id().id.toString(), listOf("doc:firstName")))
            .thenReturn(mapOf("doc:firstName" to "John"))
        whenever(DOCUMENT.definitionId()).thenReturn(
            JsonSchemaDocumentDefinitionId.existingId(
                CASE_DEFINITION_NAME,
                CaseDefinitionId.of("bName", "1.0.2")
            )
        )
    }

    @Test
    fun `should get case search for case list row`() {
        val searchRequest = SearchWithConfigRequest()
        val pageable = Pageable.ofSize(10)
        whenever(documentSearchService.search(CASE_DEFINITION_NAME, searchRequest, pageable))
            .thenReturn(PageImpl(listOf(DOCUMENT)))
        whenever(caseDefinitionService.findCaseDefinition(any()))
            .thenReturn(
                caseDefinition(
                    CaseDefinitionId.of(CASE_DEFINITION_NAME, "1.0.0"),
                )
            )

        val documentsPage = service.search(CASE_DEFINITION_NAME, searchRequest, pageable)

        assertEquals(documentsPage.content.size, 1)
        assertEquals(documentsPage.content[0].items.size, 1)
        assertEquals(documentsPage.content[0].items[0].key, "first-name")
        assertEquals(documentsPage.content[0].items[0].value, "John")
    }

    @Test
    fun `should sort on jsonpath when sorting the search`() {
        val searchRequest = SearchWithConfigRequest()
        val pageable = PageRequest.of(0, 1, Sort.by("\$.some.jsonPath"))
        whenever(documentSearchService.search(CASE_DEFINITION_NAME, searchRequest, pageable))
            .thenReturn(PageImpl(listOf(DOCUMENT)))
        whenever(caseDefinitionService.findCaseDefinition(any()))
            .thenReturn(
                caseDefinition(
                    CaseDefinitionId.of(CASE_DEFINITION_NAME, "1.0.0"),
                    CASE_DEFINITION_NAME,
                )
            )

        val documentsPage = service.search(CASE_DEFINITION_NAME, searchRequest, pageable)

        assertEquals(documentsPage.content.size, 1)
    }

    @Test
    fun `should fail to insert existing quick search when it already exists`() {
        val userId = "random-user"
        val title = "title"

        whenever(
            quickSearchRepository.existsByCaseDefinitionKeyAndUserIdAndTitle(
                CASE_DEFINITION_NAME,
                userId,
                title
            )
        ).thenReturn(
            true
        )
    }

    @Test
    fun `should succeed to insert existing quick search`() {
        val userId = "random-user"
        val title = "title"

        whenever(
            quickSearchRepository.existsByCaseDefinitionKeyAndUserIdAndTitle(
                CASE_DEFINITION_NAME,
                userId,
                title
            )
        ).thenReturn(
            false
        )

        service.storeQuickSearch(
            CASE_DEFINITION_NAME,
            CaseDefinitionQuickSearchDto("querypath", title),
            userId
        )

        verify(quickSearchRepository, times(1)).save(any())
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
