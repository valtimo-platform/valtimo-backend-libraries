package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.BaseTest
import com.ritense.authorization.AuthorizationService
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.domain.CaseListColumnId
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.domain.EmptyDisplayTypeParameter
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.service.exception.ExportLimitExceedsException
import com.ritense.case.service.exception.NoExportPermissionException
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.document.service.DocumentSearchService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.outbox.OutboxService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.ValueResolverService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals


class CaseExporterTest : BaseTest() {
    private lateinit var caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository
    private lateinit var documentSearchService: DocumentSearchService
    private lateinit var valueResolverService: ValueResolverService
    private lateinit var userManagementService: UserManagementService
    private lateinit var authorizationService: AuthorizationService
    private lateinit var jsonSchemaDocumentDefinitionService: JsonSchemaDocumentDefinitionService
    private lateinit var outboxService: OutboxService
    private lateinit var mapper: ObjectMapper
    private lateinit var exporter: CaseExporter

    @BeforeEach
    fun setUp() {
        caseDefinitionListColumnRepository = mock()
        documentSearchService = mock()
        valueResolverService = mock()
        userManagementService = mock()
        authorizationService = mock()
        jsonSchemaDocumentDefinitionService = mock()
        outboxService = mock()
        mapper = ObjectMapper()
        exporter = CaseExporter(
            caseDefinitionListColumnRepository,
            documentSearchService,
            valueResolverService,
            userManagementService,
            authorizationService,
            jsonSchemaDocumentDefinitionService,
            outboxService,
            mapper
        )

        whenever(DOCUMENT.id()).thenReturn(JsonSchemaDocumentId.newId(UUID.randomUUID()))

        whenever(caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(CASE_DEFINITION_NAME))
            .thenReturn(listOf(CREATED_ON_CASE_LIST_COLUMN, FIRST_NAME_CASE_LIST_COLUMN, LAST_NAME_CASE_LIST_COLUMN))

        whenever(
            valueResolverService.resolveValues(
                DOCUMENT.id().id.toString(),
                listOf("case:createdOn", "doc:firstName")
            )
        )
            .thenReturn(mapOf("case:createdOn" to "2025-08-26", "doc:firstName" to "John"))

        whenever(DOCUMENT.definitionId()).thenReturn(
            JsonSchemaDocumentDefinitionId.of(
                CASE_DEFINITION_NAME,
                CaseDefinitionId.of("testCaseDefinition", "1.0.0")
            )
        )

        val testUser = ValtimoUser()
        testUser.firstName = "John"
        testUser.email = "john@example.com"
        whenever(userManagementService.currentUser).thenReturn(testUser)
    }

    @Test
    fun `should return only exportable case list columns`() {
        val caseDefinitionKey = CASE_DEFINITION_NAME
        val exportableColumns = caseDefinitionListColumnRepository
            .findByIdCaseDefinitionKeyOrderByOrderAsc(caseDefinitionKey)
            .filter { it.exportable }

        assertEquals(exportableColumns.size, 2)
    }

    @Test
    fun `should get cases for exportable case list columns`() {
        val caseDefinitionKey = CASE_DEFINITION_NAME
        val searchRequest = SearchWithConfigRequest()
        val pageable = PageRequest.of(0, 10, Sort.by("case:createdOn"))

        whenever(
            documentSearchService.search(
                eq(caseDefinitionKey),
                eq(searchRequest),
                any<Pageable>()
            )
        )
            .thenReturn(PageImpl(List(10) { DOCUMENT }))

        val documentDefinition = mock<JsonSchemaDocumentDefinition>()

        whenever(jsonSchemaDocumentDefinitionService.findActiveByName(CASE_DEFINITION_NAME))
            .thenReturn(Optional.of(documentDefinition))

        whenever(authorizationService.hasPermission<Any>(any())).thenReturn(true)

        val exportableDocuments = exporter.searchExportable(caseDefinitionKey, searchRequest, pageable)

        assertEquals(10, exportableDocuments.size)
        assertEquals(2, exportableDocuments[0].items.size)
        assertEquals("created-on", exportableDocuments[0].items[0].key)
        assertEquals("2025-08-26", exportableDocuments[0].items[0].value)
    }

    @Test
    fun `should throw when maximum export limit exceeded`() {
        val caseDefinitionKey = CASE_DEFINITION_NAME
        val searchRequest = SearchWithConfigRequest()
        val pageable = PageRequest.of(0, 10, Sort.by("case:createdOn"))

        whenever(
            documentSearchService.search(
                eq(caseDefinitionKey),
                eq(searchRequest),
                any<Pageable>()
            )
        ).thenReturn(PageImpl(List(10002) { DOCUMENT }))

        whenever(authorizationService.hasPermission<Any>(any())).thenReturn(true)

        val exception = assertThrows<ExportLimitExceedsException> {
            exporter.searchExportable(caseDefinitionKey, searchRequest, pageable)
        }

        assertEquals(
            "Export failed for case '$caseDefinitionKey': the number of cases exceeds the maximum limit of 10,000. Please refine your search criteria.",
            exception.message
        )
    }

    @Test
    fun `should throw when no permission found`() {
        val caseDefinitionKey = CASE_DEFINITION_NAME
        val searchRequest = SearchWithConfigRequest()
        val pageable = PageRequest.of(0, 10, Sort.by("case:createdOn"))

        whenever(
            documentSearchService.search(
                eq(caseDefinitionKey),
                eq(searchRequest),
                any<Pageable>()
            )
        ).thenReturn(PageImpl(listOf(DOCUMENT)))

        val documentDefinition = mock<JsonSchemaDocumentDefinition>()
        whenever(jsonSchemaDocumentDefinitionService.findActiveByName(CASE_DEFINITION_NAME))
            .thenReturn(Optional.of(documentDefinition))

        whenever(authorizationService.hasPermission<Any>(any())).thenReturn(false)

        val exception = assertThrows<NoExportPermissionException> {
            exporter.searchExportable(caseDefinitionKey, searchRequest, pageable)
        }

        assertEquals("No permission found to export case '$CASE_DEFINITION_NAME'.", exception.message)
    }

    companion object {
        private const val CASE_DEFINITION_NAME = "abc-definition-name"
        private val DOCUMENT = mock<JsonSchemaDocument>()
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