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
import java.util.*

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
    }

    @Test
    fun `get id from document`() {
        val documentId = "11111111-1111-1111-1111-111111111111"
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val documentMock = mock<JsonSchemaDocument>()
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
            .withProcessBusinessKey(documentId)

        whenever(
            processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        ).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
        whenever(
                jsonSchemaDocumentService.getDocumentBy(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
        ).thenReturn(documentMock)
        documentDelegateService.getDocumentById(delegateExecutionFake)

        verify(jsonSchemaDocumentService).getDocumentBy(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
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
