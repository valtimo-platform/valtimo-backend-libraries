package com.ritense.objectsapi.taak.resolve

import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.objectsapi.taak.ProcessDocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*

internal class DocumentValueResolverTest {

    private lateinit var processDocumentService:ProcessDocumentService

    @InjectMocks
    private lateinit var documentValueResolver:DocumentValueResolver

    @BeforeEach
    internal fun setUp() {
        processDocumentService = mock(ProcessDocumentService::class.java)
        documentValueResolver = DocumentValueResolver(processDocumentService)
    }

    @Test
    fun `should resolve placeholder from process variables`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock(Document::class.java)
        `when`(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "value": true, "lastName": "Doe"}}}"""))
        doReturn(document).`when`(processDocumentService).getDocument(processInstanceId, variableScope)

        val resolvedValue = documentValueResolver.resolveValue(
            placeholder = "doc:/root/child/value",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should NOT resolve placeholder from process variables`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()
        val document = mock(Document::class.java)
        `when`(document.content()).thenReturn(JsonDocumentContent("""{"root":{"child":{"firstName":"John", "lastName": "Doe"}}}"""))
        doReturn(document).`when`(processDocumentService).getDocument(processInstanceId, variableScope)

        val resolvedValue = documentValueResolver.resolveValue(
            placeholder = "doc:/root/child/value",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )
        Assertions.assertThat(resolvedValue).isNull()
    }
}