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

package com.ritense.processdocument.resolver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.config.DocumentProperties
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.exception.ModifyDocumentException
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.dao.OptimisticLockingFailureException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DocumentJsonValueResolverFactoryPessimisticLockTest {

    private lateinit var processDocumentService: ProcessDocumentService
    private lateinit var documentService: DocumentService
    private lateinit var documentDefinitionService: JsonSchemaDocumentDefinitionService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var documentId: UUID
    private lateinit var mockDocument: JsonSchemaDocument

    @BeforeEach
    fun setUp() {
        processDocumentService = mock()
        documentService = mock()
        documentDefinitionService = mock()
        objectMapper = MapperSingleton.get()
        documentId = UUID.randomUUID()

        // Setup mock document
        mockDocument = mock {
            on { id() } doReturn JsonSchemaDocumentId.existingId(documentId)
            on { content() } doReturn JsonDocumentContent("""{"counter": 0, "name": "test"}""")
        }
    }

    @Test
    fun `should use atomic updates when pessimistic locking enabled`() {
        // Given
        val pessimisticProperties = DocumentProperties(true, 30000)
        whenever(documentService.get(documentId.toString())).thenReturn(mockDocument)

        val valueResolver = DocumentJsonValueResolverFactory(
            processDocumentService,
            documentService,
            documentDefinitionService,
            objectMapper,
            pessimisticProperties
        )

        val values = mapOf("doc:counter" to 42)

        // When
        valueResolver.handleValues(documentId, values)

        // Then - Should call updateDocumentAtomic instead of modifyDocument
        verify(documentService).updateDocumentAtomic(eq(documentId), any())
        verify(documentService, never()).modifyDocument(any(), any())
    }

    @Test
    fun `should use optimistic retry when pessimistic locking disabled`() {
        // Given
        val optimisticProperties = DocumentProperties(false, 30000)
        whenever(documentService.get(documentId.toString())).thenReturn(mockDocument)

        val valueResolver = DocumentJsonValueResolverFactory(
            processDocumentService,
            documentService,
            documentDefinitionService,
            objectMapper,
            optimisticProperties
        )

        val values = mapOf("doc:counter" to 42)

        // When
        valueResolver.handleValues(documentId, values)

        // Then - Should call modifyDocument instead of updateDocumentAtomic
        verify(documentService).modifyDocument(any(), any())
        verify(documentService, never()).updateDocumentAtomic(any(), any())
    }

    @Test
    fun `should retry on OptimisticLockingFailureException`() {
        // Given
        val optimisticProperties = DocumentProperties(false, 30000)
        whenever(documentService.get(documentId.toString())).thenReturn(mockDocument)

        var attemptCount = 0
        whenever(documentService.modifyDocument(any(), any())).thenAnswer {
            attemptCount++
            if (attemptCount <= 2) {
                val optimisticException = OptimisticLockingFailureException("Simulated failure")
                val modifyException = ModifyDocumentException(mutableListOf())
                modifyException.initCause(optimisticException)
                throw modifyException
            }
            // Third attempt succeeds
            Unit
        }

        val valueResolver = DocumentJsonValueResolverFactory(
            processDocumentService,
            documentService,
            documentDefinitionService,
            objectMapper,
            optimisticProperties
        )

        val values = mapOf("doc:counter" to 42)

        // When
        val startTime = System.currentTimeMillis()
        valueResolver.handleValues(documentId, values)
        val endTime = System.currentTimeMillis()

        // Then
        assertEquals(3, attemptCount, "Should attempt 3 times before succeeding")
        verify(documentService, times(3)).modifyDocument(any(), any())
    }

    @Test
    fun `should fail after max retry attempts`() {
        // Given
        val optimisticProperties = DocumentProperties(false, 30000)
        whenever(documentService.get(documentId.toString())).thenReturn(mockDocument)
        whenever(documentService.modifyDocument(any(), any())).thenAnswer {
            val optimisticException = OptimisticLockingFailureException("Persistent failure")
            val modifyException = ModifyDocumentException(mutableListOf())
            modifyException.initCause(optimisticException)
            throw modifyException
        }

        val valueResolver = DocumentJsonValueResolverFactory(
            processDocumentService,
            documentService,
            documentDefinitionService,
            objectMapper,
            optimisticProperties
        )

        val values = mapOf("doc:counter" to 42)

        // When & Then
        val exception = assertFailsWith<RuntimeException> {
            valueResolver.handleValues(documentId, values)
        }

        assertEquals(true, exception.message!!.contains("Attempts: 3"))
        verify(documentService, times(3)).modifyDocument(any(), any())
    }
}