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

package com.ritense.valtimo.formflow

import com.ritense.document.service.DocumentService
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.FormLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.service.FormAssociationService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.task.Task
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

internal class FormFlowProcessLinkTaskProviderTest {

    private lateinit var formFlowService: FormFlowService
    private lateinit var formAssociationService: FormAssociationService
    private lateinit var documentService: DocumentService
    private lateinit var repositoryService: RepositoryService
    private lateinit var runtimeService: RuntimeService
    private lateinit var formFlowProcessLinkTaskProvider: FormFlowProcessLinkTaskProvider

    @BeforeEach
    fun beforeEach() {
        formFlowService = mock()
        formAssociationService = mock()
        documentService = mock()
        repositoryService = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        runtimeService = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        formFlowProcessLinkTaskProvider = FormFlowProcessLinkTaskProvider(
            formFlowService,
            formAssociationService,
            documentService,
            repositoryService,
            runtimeService,
        )
    }

    @Test
    fun `supports only bpmnElementFormFlowIdLink`() {
        val formLink1: BpmnElementFormFlowIdLink = mock()
        val formLink2: FormLink = mock()
        assertTrue(formFlowProcessLinkTaskProvider.supports(formLink1))
        assertFalse(formFlowProcessLinkTaskProvider.supports(formLink2))
    }

    @Test
    fun `getTaskResult contains formFlowId `() {
        val formLink: BpmnElementFormFlowIdLink = mock()
        val task: Task = mock()
        val formFlowInstance: FormFlowInstance = mock()
        val formFlowInstanceId = FormFlowInstanceId.newId()
        whenever(formLink.formFlowId).thenReturn("123")
        whenever(formFlowService.findInstances(any())).thenReturn(listOf(formFlowInstance))
        whenever(formFlowInstance.id).thenReturn(formFlowInstanceId)

        val taskResult = formFlowProcessLinkTaskProvider.getTaskResult(task, formLink)

        assertEquals("form-flow", taskResult.type)
        assertEquals(formFlowInstanceId.id, taskResult.properties.formFlowInstanceId)
    }

    @Test
    fun `intercepts CreateTaskCmd and handle correctly`() {
        whenever(runtimeService.createProcessInstanceQuery().processInstanceId(any()).singleResult())
            .thenReturn(mock())
        whenever(repositoryService.createProcessDefinitionQuery().processDefinitionId(any()).singleResult())
            .thenReturn(mock())
        val task: Task = mock()
        val formFlowDefinition: FormFlowDefinition = mock()
        val formLink: BpmnElementFormFlowIdLink = mock()
        val formAssociation: FormAssociation = mock()
        val formFlowInstance: FormFlowInstance = mock()
        whenever(formLink.formFlowId).thenReturn("abc:1")
        whenever(formAssociation.formLink).thenReturn(formLink)
        whenever(formFlowInstance.id).thenReturn(FormFlowInstanceId(UUID.randomUUID()))
        whenever(formAssociationService
            .getFormAssociationByFormLinkId(null, null)).thenReturn(Optional.of(formAssociation))
        whenever(formFlowService.findDefinition(any())).thenReturn(formFlowDefinition)
        whenever(formFlowService.save(formFlowInstance)).thenReturn(formFlowInstance)
        whenever(formFlowDefinition.createInstance(any())).thenReturn(formFlowInstance)

        formFlowProcessLinkTaskProvider.getTaskResult(task, formLink)

        verify(formFlowService).save(formFlowInstance)
    }
}
