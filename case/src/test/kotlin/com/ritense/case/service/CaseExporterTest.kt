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
import com.ritense.BaseTest
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.domain.CaseListColumnId
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.service.exception.ExportLimitExceedsException
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.impl.JsonSchemaDocumentSearchService
import com.ritense.outbox.OutboxService
import com.ritense.search.domain.DisplayType
import com.ritense.search.domain.EmptyDisplayTypeParameter
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.text.Charsets.UTF_8

class CaseExporterTest : BaseTest() {
    private lateinit var caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository
    private lateinit var documentSearchService: JsonSchemaDocumentSearchService
    private lateinit var outboxService: OutboxService
    private lateinit var mapper: ObjectMapper
    private lateinit var caseListRowMapper: CaseListRowMapper
    private lateinit var exporter: CaseExporter

    @BeforeEach
    fun setUp() {
        caseDefinitionListColumnRepository = mock()
        documentSearchService = mock()
        outboxService = mock()
        mapper = ObjectMapper()
        caseListRowMapper = mock()
        exporter = CaseExporter(
            caseDefinitionListColumnRepository,
            documentSearchService,
            outboxService,
            mapper,
            caseListRowMapper,
        )

        whenever(DOCUMENT.id()).thenReturn(JsonSchemaDocumentId.newId(UUID.randomUUID()))

        whenever(
            caseDefinitionListColumnRepository
                .findByIdCaseDefinitionKeyOrderByOrderAsc(CASE_DEFINITION_NAME)
        )
            .thenReturn(listOf(CREATED_ON_CASE_LIST_COLUMN, FIRST_NAME_CASE_LIST_COLUMN, LAST_NAME_CASE_LIST_COLUMN))

        whenever(DOCUMENT.definitionId()).thenReturn(
            JsonSchemaDocumentDefinitionId.of(
                CASE_DEFINITION_NAME,
                CaseDefinitionId.of("testCaseDefinition", "1.0.0")
            )
        )
    }

    @Test
    fun `should return only exportable case list columns`() {
        val exportableColumns = caseDefinitionListColumnRepository
            .findByIdCaseDefinitionKeyOrderByOrderAsc(CASE_DEFINITION_NAME)
            .filter { it.exportable }

        assertEquals(exportableColumns.size, 2)
    }

    @Test
    fun `should export cases as csv response`() {
        val pageable = PageRequest.of(0, 25, Sort.by("created-on").descending())
        val searchRequest = SearchWithConfigRequest()

        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(CASE_DEFINITION_NAME)
                .filter { it.exportable })
            .thenReturn(listOf(CREATED_ON_CASE_LIST_COLUMN, FIRST_NAME_CASE_LIST_COLUMN))

        val documentDefinition = mock<JsonSchemaDocumentDefinition>()

        val docs = listOf(DOCUMENT, DOCUMENT2, DOCUMENT3)

        whenever(
            documentSearchService.searchForExport(eq(CASE_DEFINITION_NAME), eq(searchRequest), any())
        ).thenReturn(PageImpl(docs))

        whenever(caseListRowMapper.toCaseListRowDto(eq(docs[0]), any())).thenReturn(
            CaseListRowDto(
                "doc-1", listOf(
                    CaseListRowDto.CaseListItemDto("created-on", "2025-09-01T10:00:00"),
                    CaseListRowDto.CaseListItemDto("first-name", "Alex")
                )
            )
        )
        whenever(caseListRowMapper.toCaseListRowDto(eq(docs[1]), any())).thenReturn(
            CaseListRowDto(
                "doc-2", listOf(
                    CaseListRowDto.CaseListItemDto("created-on", "2025-09-01T10:00:01"),
                    CaseListRowDto.CaseListItemDto("first-name", "Bob")
                )
            )
        )
        whenever(caseListRowMapper.toCaseListRowDto(eq(docs[2]), any())).thenReturn(
            CaseListRowDto(
                "doc-3", listOf(
                    CaseListRowDto.CaseListItemDto("created-on", "2025-09-01T10:00:02"),
                    CaseListRowDto.CaseListItemDto("first-name", "Charlie")
                )
            )
        )

        val resp: ResponseEntity<ByteArray> =
            exporter.exportCases(CASE_DEFINITION_NAME, searchRequest, pageable)

        val contentType = resp.headers.contentType?.toString() ?: ""
        assertTrue(contentType.startsWith("text/csv"))

        val cd = resp.headers.getFirst("Content-Disposition") ?: ""
        assertTrue(cd.contains("""attachment;"""))
        assertTrue(cd.contains("${CASE_DEFINITION_NAME}_cases_export_${LocalDate.now()}"))


        val csv = String(resp.body!!, UTF_8)

        assertEquals(4, csv.lines().filter { it.isNotBlank() }.size)

        assertTrue(csv.lines().first().contains("created-on"))
        assertTrue(csv.lines().first().contains("first-name"))

        assertTrue(csv.contains("2025-09-01T10:00:00"))
        assertTrue(csv.contains("2025-09-01T10:00:01"))
        assertTrue(csv.contains("Alex"))
        assertTrue(csv.contains("Charlie"))

        verify(outboxService, times(1)).send(any())
    }

    @Test
    fun `should throw when maximum export limit exceeded`() {
        val searchRequest = SearchWithConfigRequest()
        val pageable = PageRequest.of(0, 10000, Sort.by("case:createdOn"))

        whenever(
            documentSearchService.searchForExport(
                eq(CASE_DEFINITION_NAME),
                eq(searchRequest),
                any<Pageable>()
            )
        ).thenReturn(PageImpl(List(10002) { DOCUMENT }))

        val exception = assertThrows<ExportLimitExceedsException> {
            exporter.exportCases(CASE_DEFINITION_NAME, searchRequest, pageable)
        }

        assertEquals(
            "Export failed for case '$CASE_DEFINITION_NAME': the number of cases exceeds the maximum limit of 10,000. Please refine your search criteria.",
            exception.message
        )

        verify(outboxService, never()).send(any())
    }

    companion object {
        private const val CASE_DEFINITION_NAME = "abc-definition-name"
        private val DOCUMENT = mock<JsonSchemaDocument>()
        private val DOCUMENT2 = mock<JsonSchemaDocument>()
        private val DOCUMENT3 = mock<JsonSchemaDocument>()
        private val CREATED_ON_CASE_LIST_COLUMN = CaseListColumn(
            id = CaseListColumnId(CASE_DEFINITION_NAME, "created-on"),
            title = "Created on",
            path = "case:createdOn",
            displayType = DisplayType("date", EmptyDisplayTypeParameter()),
            sortable = true,
            defaultSort = ColumnDefaultSort.DESC,
            order = 0,
            exportable = true,
        )
        private val FIRST_NAME_CASE_LIST_COLUMN = CaseListColumn(
            id = CaseListColumnId(CASE_DEFINITION_NAME, "first-name"),
            title = "First name",
            path = "doc:firstName",
            displayType = DisplayType("text", EmptyDisplayTypeParameter()),
            sortable = true,
            defaultSort = null,
            order = 1,
            exportable = true,
        )
        private val LAST_NAME_CASE_LIST_COLUMN = CaseListColumn(
            id = CaseListColumnId(CASE_DEFINITION_NAME, "last-name"),
            title = "Last name",
            path = "doc:lastName",
            displayType = DisplayType("text", EmptyDisplayTypeParameter()),
            sortable = true,
            defaultSort = null,
            order = 2,
            exportable = false,
        )
    }
}