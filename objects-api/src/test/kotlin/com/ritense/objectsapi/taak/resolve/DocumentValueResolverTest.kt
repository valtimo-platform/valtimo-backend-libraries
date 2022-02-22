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

package com.ritense.objectsapi.taak.resolve

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.patch.operation.AddOperation
import com.ritense.valtimo.contract.json.patch.operation.JsonPatchOperation
import com.ritense.valtimo.contract.json.patch.operation.ReplaceOperation
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DocumentValueResolverTest {

    private lateinit var processDocumentService: ProcessDocumentService
    private lateinit var documentService: DocumentService

    private lateinit var documentValueResolver: DocumentValueResolverFactory

    @BeforeEach
    internal fun setUp() {
        processDocumentService = mock()
        documentService = mock()
        documentValueResolver = DocumentValueResolverFactory(processDocumentService, documentService)
    }

    @Test
    fun `should resolve boolean value from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "value": true, "lastName": "Doe"}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/value"
        )

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should resolve string value from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/firstName"
        )

        Assertions.assertThat(resolvedValue).isEqualTo("John")
    }

    @Test
    fun `should resolve int value from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe", "age": 5}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/age"
        )

        Assertions.assertThat(resolvedValue).isEqualTo(5)
    }

    @Test
    fun `should NOT resolve requestedValue from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/value"
        )

        Assertions.assertThat(resolvedValue).isNull()
    }

    @Test
    fun `should add text value`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("add:/firstname" to "John")
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        Assertions.assertThat(captor.firstValue.content()).contains(TextNode.valueOf("John"))
    }

    @Test
    fun `should replace text value`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("replace:/firstname" to "John")
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        Assertions.assertThat(captor.firstValue.content()).contains(TextNode.valueOf("John"))
    }

    @Test
    fun `should add boolean value`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("add:/approved" to true)
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        Assertions.assertThat(captor.firstValue.content()).contains(BooleanNode.TRUE)
    }

    @Test
    fun `should add int value`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("add:/age" to 18)
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        Assertions.assertThat(captor.firstValue.content()).contains(IntNode.valueOf(18))
    }
}