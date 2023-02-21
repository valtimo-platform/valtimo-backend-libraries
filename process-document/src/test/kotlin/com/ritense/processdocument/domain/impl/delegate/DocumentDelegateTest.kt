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

package com.ritense.processdocument.domain.impl.delegate

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import org.camunda.community.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

internal class DocumentDelegateTest {

    lateinit var processDocumentService: ProcessDocumentService
    lateinit var userManagementService: UserManagementService
    lateinit var documentService: DocumentService
    lateinit var documentDelegate: DocumentDelegate

    @BeforeEach
    fun beforeEach() {
        processDocumentService = mock()
        userManagementService = mock()
        documentService = mock()
        documentDelegate = DocumentDelegate(
            processDocumentService,
            userManagementService,
            documentService,
        )
    }

    @Test
    fun `should assign user to document`() {
        val documentId = "11111111-1111-1111-1111-111111111111"
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
            .withProcessBusinessKey(documentId)
        whenever(
            processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        ).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
        whenever(userManagementService.findByEmail("john@example.com"))
            .thenReturn(Optional.of(ValtimoUserBuilder().id("anId").build()))

        documentDelegate.setAssignee(delegateExecutionFake, "john@example.com")

        verify(documentService, times(1)).assignUserToDocument(UUID.fromString(documentId), "anId")
    }

    @Test
    fun `should unassign user from document`() {
        val documentId = "11111111-1111-1111-1111-111111111111"
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
            .withProcessBusinessKey(documentId)
        whenever(
            processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        ).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))

        documentDelegate.unassign(delegateExecutionFake)

        verify(documentService, times(1)).unassignUserFromDocument(UUID.fromString(documentId))
    }
}
