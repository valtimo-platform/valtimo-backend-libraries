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

package com.ritense.processdocument.service

import com.ritense.document.domain.impl.*
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.BaseTest
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.community.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID
import java.util.Optional


internal class DocumentDelegateServiceTest : BaseTest() {

    lateinit var processDocumentService: ProcessDocumentService
    lateinit var documentService: DocumentService
    lateinit var jsonSchemaDocumentService: JsonSchemaDocumentService
    lateinit var documentDelegateService: DocumentDelegateService

    lateinit var definition: JsonSchemaDocumentDefinition
    lateinit var delegateExecutionFake: DelegateExecution

    private val STREET_NAME = "street"
    private val HOUSE_NUMBER = "3"
    private val NO = false

    private val documentId = "11111111-1111-1111-1111-111111111111"
    private val processInstanceId = "00000000-0000-0000-0000-000000000000"

    @BeforeEach
    fun setup() {
        definition = definition()
        documentSequenceGeneratorService = mock()
        whenever(documentSequenceGeneratorService.next(any())).thenReturn(1L)
        processDocumentService = mock()
        documentService = mock()
        jsonSchemaDocumentService = mock()
        documentDelegateService = DocumentDelegateService(
            processDocumentService,
            documentService,
            jsonSchemaDocumentService
        )
        delegateExecutionFake =
            DelegateExecutionFake("id").withProcessBusinessKey("56f29315-c581-4c26-9b70-8bc818e8c86e");
    }

    @Test
    fun `get modifiedOn from document`() {
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)

        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
        val modifiedOn = LocalDateTime.now()

        whenever(documentMock.modifiedOn()).thenReturn(Optional.of(modifiedOn))
        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)

        val modifiedOnResult = documentDelegateService.getDocumentModifiedOn(delegateExecutionFake)

        assertEquals(modifiedOnResult, modifiedOn)

        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `get assigneeId from document`() {
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
        val assigneeId = "1234"

        whenever(documentMock.assigneeId()).thenReturn(assigneeId)
        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)

        val assigneeIdResult = documentDelegateService.getDocumentAssigneeId(delegateExecutionFake)

        assertEquals(assigneeIdResult, assigneeId)

        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `get createdBy from document`() {
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
        val createdBy = "Pietersen"

        whenever(documentMock.createdBy()).thenReturn(createdBy)
        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)

        val createdByResult = documentDelegateService.getDocumentCreatedBy(delegateExecutionFake)

        assertEquals(createdByResult, createdBy)

        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `get fullname assignee from document`() {
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
        val assigneeFullname = "Jan Jansen"

        whenever(documentMock.assigneeFullName()).thenReturn(assigneeFullname)
        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)

        val assigneFullNameResult = documentDelegateService.getDocumentAssigneeFullName(delegateExecutionFake)

        assertEquals(assigneFullNameResult, assigneeFullname)

        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `get version from document`() {
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
        var version = documentMock.version();

        whenever(documentMock.version()).thenReturn(version)
        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)

        val versionResult = documentDelegateService.getDocumentVersion(delegateExecutionFake)

        assertEquals(versionResult, version)

        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `get createdOn from document`() {
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
        val createdOn = LocalDateTime.now()

        whenever(documentMock.createdOn()).thenReturn(createdOn)
        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)

        val createdOnResult = documentDelegateService.getDocumentCreatedOn(delegateExecutionFake)

        assertEquals(createdOnResult, createdOn)

        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `get document by id`() {
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)

        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))

        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)

        val resultDocument = documentDelegateService.getDocumentById(delegateExecutionFake)

        assertEquals(documentMock, resultDocument)

        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `find value by json pointer`() {
        val jsonSchemaDocument = createDocument()

        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any = documentDelegateService.findValueByJsonPointer(
            "/applicant/number", delegateExecutionFake
        )

        assertEquals(HOUSE_NUMBER, value)
    }

    @Test
    fun `incorrect path should return default value`() {
        val jsonSchemaDocument = createDocument()
        val defaultValue = "DEFAULT_VALUE"
        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any = documentDelegateService.findValueByJsonPointerOrDefault(
            "/incorrectpath", delegateExecutionFake, defaultValue
        )

        assertEquals(defaultValue, value)
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
