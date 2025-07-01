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

package com.ritense.processdocument.service

import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.BaseTest
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId
import com.ritense.valtimo.contract.OauthConfigHolder
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import com.ritense.valtimo.contract.config.ValtimoProperties.Oauth
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID


internal class DocumentDelegateServiceTest : BaseTest() {

    private lateinit var processDocumentService: ProcessDocumentService
    private lateinit var documentService: DocumentService
    private lateinit var jsonSchemaDocumentService: JsonSchemaDocumentService
    private lateinit var userManagementService: UserManagementService
    private lateinit var documentDelegateService: DocumentDelegateService

    lateinit var definition: JsonSchemaDocumentDefinition
    private lateinit var delegateExecution: DelegateExecution

    private val documentId = "11111111-1111-1111-1111-111111111111"
    private val processInstanceId = "00000000-0000-0000-0000-000000000000"

    private val documentMock = mock<JsonSchemaDocument>()
    private val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))

    companion object {
        private const val STREET_NAME = "street"
        private const val HOUSE_NUMBER = "3"
        private const val NO = false
    }

    @BeforeEach
    fun setup() {
        definition = definition()
        documentSequenceGeneratorService = mock()
        whenever(documentSequenceGeneratorService.next(any())).thenReturn(1L)
        processDocumentService = mock()
        userManagementService = mock()
        documentService = mock()
        jsonSchemaDocumentService = mock()
        jsonSchemaDocumentService = mock()
        documentDelegateService = DocumentDelegateService(
            processDocumentService,
            documentService,
            jsonSchemaDocumentService,
            userManagementService,
            MapperSingleton.get()
        )
        delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processBusinessKey).thenReturn("56f29315-c581-4c26-9b70-8bc818e8c86e")

        OauthConfigHolder(Oauth())
    }

    @Test
    fun `get modifiedOn from document`() {
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        val modifiedOn = LocalDateTime.now()

        whenever(documentMock.modifiedOn()).thenReturn(Optional.of(modifiedOn))
        prepareDocument(processDocumentService, delegateExecution, jsonSchemaDocumentService)

        val modifiedOnResult = documentDelegateService.getDocumentModifiedOn(delegateExecution)

        assertEquals(modifiedOnResult, modifiedOn)
        verifyTest(processDocumentService, delegateExecution, jsonSchemaDocumentService)
    }

    @Test
    fun `get assigneeId from document`() {
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        val assigneeId = "1234"

        whenever(documentMock.assigneeId()).thenReturn(assigneeId)
        prepareDocument(processDocumentService, delegateExecution, jsonSchemaDocumentService)

        val assigneeIdResult = documentDelegateService.getDocumentAssigneeId(delegateExecution)

        assertEquals(assigneeIdResult, assigneeId)
        verifyTest(processDocumentService, delegateExecution, jsonSchemaDocumentService)
    }

    @Test
    fun `get createdBy from document`() {
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        val createdBy = "Pietersen"

        whenever(documentMock.createdBy()).thenReturn(createdBy)
        prepareDocument(processDocumentService, delegateExecution, jsonSchemaDocumentService)

        val createdByResult = documentDelegateService.getDocumentCreatedBy(delegateExecution)

        assertEquals(createdByResult, createdBy)
        verifyTest(processDocumentService, delegateExecution, jsonSchemaDocumentService)
    }

    @Test
    fun `get fullname assignee from document`() {
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        val assigneeFullname = "Jan Jansen"

        whenever(documentMock.assigneeFullName()).thenReturn(assigneeFullname)
        prepareDocument(processDocumentService, delegateExecution, jsonSchemaDocumentService)

        val assigneFullNameResult = documentDelegateService.getDocumentAssigneeFullName(delegateExecution)

        assertEquals(assigneFullNameResult, assigneeFullname)
        verifyTest(processDocumentService, delegateExecution, jsonSchemaDocumentService)
    }

    @Test
    fun `get version from document`() {
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        val version = documentMock.version()

        whenever(documentMock.version()).thenReturn(version)
        prepareDocument(processDocumentService, delegateExecution, jsonSchemaDocumentService)

        val versionResult = documentDelegateService.getDocumentVersion(delegateExecution)

        assertEquals(versionResult, version)
        verifyTest(processDocumentService, delegateExecution, jsonSchemaDocumentService)
    }

    @Test
    fun `get createdOn from document`() {
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        val createdOn = LocalDateTime.now()

        whenever(documentMock.createdOn()).thenReturn(createdOn)
        prepareDocument(processDocumentService, delegateExecution, jsonSchemaDocumentService)

        val createdOnResult = documentDelegateService.getDocumentCreatedOn(delegateExecution)

        assertEquals(createdOnResult, createdOn)
        verifyTest(processDocumentService, delegateExecution, jsonSchemaDocumentService)
    }

    @Test
    fun `get document by execution`() {
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)

        prepareDocument(processDocumentService, delegateExecution, jsonSchemaDocumentService)

        val resultDocument = documentDelegateService.getDocument(delegateExecution)

        assertEquals(documentMock, resultDocument)
        verifyTest(processDocumentService, delegateExecution, jsonSchemaDocumentService)
    }

    private fun prepareDocument(processDocumentService: ProcessDocumentService,
                                delegateExecution: DelegateExecution,
                                jsonSchemaDocumentService: JsonSchemaDocumentService) {
        whenever(
            processDocumentService.getDocumentId(
                OperatonProcessInstanceId(processInstanceId),
                delegateExecution
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)
    }

    private fun verifyTest(processDocumentService: ProcessDocumentService,
                           delegateExecution: DelegateExecution,
                           jsonSchemaDocumentService: JsonSchemaDocumentService) {
        verify(processDocumentService).getDocumentId(OperatonProcessInstanceId(processInstanceId), delegateExecution)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `find value by json pointer`() {
        val jsonSchemaDocument = createDocument()

        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any = documentDelegateService.findValueByJsonPointer(
            "/applicant/number", delegateExecution
        )

        assertEquals(HOUSE_NUMBER, value)
    }

    @Test
    fun `incorrect path should return default value`() {
        val jsonSchemaDocument = createDocument()
        val defaultValue = "DEFAULT_VALUE"
        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any? = documentDelegateService.findValueByJsonPointerOrDefault(
            "/incorrectpath", delegateExecution, defaultValue
        )

        assertEquals(defaultValue, value)
    }

    @Test
    fun `should accept null for default value`() {
        val jsonSchemaDocument = createDocument()
        val defaultValue = null
        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any? = documentDelegateService.findValueByJsonPointerOrDefault(
            "/incorrectpath", delegateExecution, defaultValue
        )

        assertEquals(defaultValue, value)
    }

    @Test
    fun `should assign user to document`() {
        val documentId = "11111111-1111-1111-1111-111111111111"
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        whenever(delegateExecution.processBusinessKey).thenReturn(documentId)
        whenever(
            processDocumentService.getDocumentId(OperatonProcessInstanceId(processInstanceId), delegateExecution)
        ).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
        whenever(userManagementService.findByEmail("john@example.com"))
            .thenReturn(Optional.of(ValtimoUserBuilder().id("anId").build()))

        documentDelegateService.setAssignee(delegateExecution, "john@example.com")

        verify(documentService, times(1)).assignUserToDocument(UUID.fromString(documentId), "anId")
    }

    @Test
    fun `should set status to document`() {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        whenever(delegateExecution.processBusinessKey).thenReturn(documentId.toString())
        whenever(
            processDocumentService.getDocumentId(OperatonProcessInstanceId(processInstanceId), delegateExecution)
        ).thenReturn(documentId)

        val newStatus = "test"
        documentDelegateService.setInternalStatus(delegateExecution, newStatus)

        verify(documentService).setInternalStatus(documentId, newStatus)
    }

    @Test
    fun `should add case tag to document`() {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        whenever(delegateExecution.processBusinessKey).thenReturn(documentId.toString())
        whenever(
            processDocumentService.getDocumentId(OperatonProcessInstanceId(processInstanceId), delegateExecution)
        ).thenReturn(documentId)

        documentDelegateService.addCaseTag(delegateExecution, "important")

        verify(documentService).addCaseTag(documentId, "important")
    }

    @Test
    fun `should remove case tag from document`() {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        whenever(delegateExecution.processBusinessKey).thenReturn(documentId.toString())
        whenever(
            processDocumentService.getDocumentId(OperatonProcessInstanceId(processInstanceId), delegateExecution)
        ).thenReturn(documentId)

        documentDelegateService.removeCaseTag(delegateExecution, "important")

        verify(documentService).removeCaseTag(documentId, "important")
    }

    @Test
    fun `should unassign user from document`() {
        val documentId = "11111111-1111-1111-1111-111111111111"
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecution = mock<DelegateExecution>()
        whenever(delegateExecution.id).thenReturn("id")
        whenever(delegateExecution.processInstanceId).thenReturn(processInstanceId)
        whenever(delegateExecution.processBusinessKey).thenReturn(documentId)
        whenever(
            processDocumentService.getDocumentId(OperatonProcessInstanceId(processInstanceId), delegateExecution)
        ).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))

        documentDelegateService.unassign(delegateExecution)

        verify(documentService, times(1)).unassignUserFromDocument(UUID.fromString(documentId))
    }


    private fun createDocument(): JsonSchemaDocument {
        return JsonSchemaDocument.create(
            definition, JsonDocumentContent(
                """
                {
                    "applicant": {
                        "street": "$STREET_NAME",
                        "number": "$HOUSE_NUMBER",
                        "prettyHouse": "$NO"
                    },
                    "cars":[
                        { "mark":"volvo", "year": 1991 },
                        { "mark":"audi", "year": 2016 }
                    ]
                }
            """.trimIndent()
            ),
            "USERNAME",
            documentSequenceGeneratorService,
            null
        ).resultingDocument().get()
    }
}
