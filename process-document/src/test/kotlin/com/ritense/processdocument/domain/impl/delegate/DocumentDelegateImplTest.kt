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

package com.ritense.processdocument.domain.impl.delegate

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

internal class DocumentDelegateImplTest {

    lateinit var processDocumentService: ProcessDocumentService
    lateinit var userManagementService: UserManagementService
    lateinit var documentService: DocumentService
    lateinit var documentDelegateImpl: DocumentDelegateImpl

    @BeforeEach
    fun beforeEach() {
        processDocumentService = mock()
        userManagementService = mock()
        documentService = mock()
        documentDelegateImpl = DocumentDelegateImpl(
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

        documentDelegateImpl.setAssignee(delegateExecutionFake, "john@example.com")

        verify(documentService, times(1)).assignUserToDocument(UUID.fromString(documentId), "anId")
    }
}
