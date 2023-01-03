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

package com.ritense.processdocument.resolver

import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DocumentValueResolverTest {

    private lateinit var processDocumentService: ProcessDocumentService
    private lateinit var documentService: DocumentService

    private lateinit var documentValueResolver: DocumentValueResolverFactory

    private lateinit var processInstanceId: String
    private lateinit var variableScope: DelegateTaskFake
    private lateinit var document: Document

    @BeforeEach
    internal fun setUp() {
        processDocumentService = mock()
        documentService = mock()
        documentValueResolver = DocumentValueResolverFactory(processDocumentService, documentService)

        processInstanceId = UUID.randomUUID().toString()
        variableScope = DelegateTaskFake()
        document = mock()
        whenever(processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), variableScope)).thenReturn(document)
    }

    @Test
    fun `should resolve boolean value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "value": true, "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/value"
        )

        assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should resolve string value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/firstName"
        )

        assertThat(resolvedValue).isEqualTo("John")
    }

    @Test
    fun `should resolve int value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe", "age": 5}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/age"
        )

        assertThat(resolvedValue).isEqualTo(5)
    }

    @Test
    fun `should NOT resolve requestedValue from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root/child/value"
        )

        assertThat(resolvedValue).isNull()
    }

    @Test
    fun `should resolve object-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"profile":{"firstName":"John"}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/profile"
        )

        assertThat(resolvedValue).isEqualTo(mapOf("firstName" to "John"))
    }

    @Test
    fun `should resolve array-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"cities":[{"name":"Amsterdam"},{"name":"Utrecht"}]}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/cities"
        )

        assertThat(resolvedValue).isEqualTo(listOf(mapOf("name" to "Amsterdam"), mapOf("name" to "Utrecht")))
    }

    @Test
    fun `should resolve empty-object-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{}}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(emptyMap<String, Any>())
    }

    @Test
    fun `should resolve empty-array-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":[]}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(emptyList<Any>())
    }

    @Test
    fun `should resolve null-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":null}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(null)
    }

    @Test
    fun `should resolve missing-value from document properties`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("""{}"""))

        val resolvedValue = documentValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            "/root"
        )

        assertThat(resolvedValue).isEqualTo(null)
    }

    @Test
    fun `should add text value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/firstname" to "John")
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        assertThat(captor.firstValue.content()).contains(TextNode.valueOf("John"))
    }

    @Test
    fun `should replace text value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"firstname\":\"Peter\"}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/firstname" to "John")
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        assertThat(captor.firstValue.content()).contains(TextNode.valueOf("John"))
    }

    @Test
    fun `should add boolean value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/approved" to true)
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        assertThat(captor.firstValue.content()).contains(BooleanNode.TRUE)
    }

    @Test
    fun `should add int value`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/age" to 18)
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        assertThat(captor.firstValue.content()).contains(IntNode.valueOf(18))
    }

    @Test
    fun `should create JsonArray and JsonObject if not exist`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/a/-/b/-/c/-/firstname" to "John")
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        assertThat(captor.firstValue.content().at("/a/0/b/0/c/0/firstname")).isEqualTo(TextNode.valueOf("John"))
    }

    @Test
    fun `should replace value in list`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"myList\":[\"Peter\"]}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/myList/0" to "John")
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        assertThat(captor.firstValue.content().at("/myList/0")).isEqualTo(TextNode.valueOf("John"))
        assertThat(captor.firstValue.content().at("/myList/1")).isEqualTo(MissingNode.getInstance())
    }

    @Test
    fun `should add value to list`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"myList\":[\"Peter\"]}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/myList/-" to "John")
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        assertThat(captor.firstValue.content().at("/myList/0")).isEqualTo(TextNode.valueOf("Peter"))
        assertThat(captor.firstValue.content().at("/myList/1")).isEqualTo(TextNode.valueOf("John"))
    }

    @Test
    fun `should add object to list`() {
        whenever(document.content()).thenReturn(JsonDocumentContent("{\"myList\":[]}"))

        documentValueResolver.handleValues(
            processInstanceId = processInstanceId,
            variableScope = variableScope,
            mapOf("/myList/-" to mapOf("field" to "My field", "list" to listOf("My item 1", "My item 2")))
        )

        val captor = argumentCaptor<ModifyDocumentRequest>()
        verify(documentService).modifyDocument(captor.capture())
        val objectNode = jacksonObjectMapper().readTree("{\"field\":\"My field\",\"list\":[\"My item 1\",\"My item 2\"]}")
        assertThat(captor.firstValue.content().at("/myList/0")).isEqualTo(objectNode)
    }
}
