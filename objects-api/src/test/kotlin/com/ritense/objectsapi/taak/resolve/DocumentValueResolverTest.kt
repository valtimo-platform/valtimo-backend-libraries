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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import org.assertj.core.api.Assertions
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class DocumentValueResolverTest {

    private lateinit var processDocumentService: ProcessDocumentService

    private lateinit var documentValueResolver: DocumentValueResolver

    @BeforeEach
    internal fun setUp() {
        processDocumentService = mock()
        documentValueResolver = DocumentValueResolver(processDocumentService)
    }

    @Test
    fun `should resolve boolean placeholder from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "value": true, "lastName": "Doe"}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.resolveValue(
            placeholder = "doc:/root/child/value",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should resolve string placeholder from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.resolveValue(
            placeholder = "doc:/root/child/firstName",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo("John")
    }

    @Test
    fun `should resolve int placeholder from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe", "age": 5}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.resolveValue(
            placeholder = "doc:/root/child/age",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo(5)
    }

    @Test
    fun `should NOT resolve placeholder from document properties`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock<Document>()
        whenever(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))
        whenever(processDocumentService.getDocument(processInstanceId, variableScope)).thenReturn(document)

        val resolvedValue = documentValueResolver.resolveValue(
            placeholder = "doc:/root/child/value",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )
        Assertions.assertThat(resolvedValue).isNull()
    }
}