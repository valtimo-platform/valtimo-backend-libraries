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

package com.ritense.processdocument.service

import com.ritense.processdocument.domain.ProcessDocumentInstance
import com.ritense.processdocument.domain.ProcessDocumentInstanceId
import com.ritense.valtimo.contract.event.DocumentDeletedEvent
import com.ritense.valtimo.contract.result.FunctionResult
import com.ritense.valtimo.contract.result.OperationError
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

class ProcessDocumentDeletedEventListenerTest {

    var processDocumentDeletedEventListener: ProcessDocumentDeletedEventListener? = null
    var runtimeService = mock<RuntimeService>(RETURNS_DEEP_STUBS)
    var processDocumentAssociationService = mock<ProcessDocumentAssociationService>(RETURNS_DEEP_STUBS)

    @BeforeEach
    fun setUp() {
        processDocumentDeletedEventListener = ProcessDocumentDeletedEventListener(runtimeService, processDocumentAssociationService)
    }


    @Test
    fun `should delete process instances with business key`() {
        val documentId = UUID.fromString("d1f1b3ed-7575-45bb-a02b-18f378ddc34d")

        val processInstance1 = mock<ProcessInstance>()
        val processInstanceId1 = "4320f9c0-5568-4ed2-91f9-d2c85fd4ce55"
        whenever(processInstance1.processInstanceId).thenReturn(processInstanceId1)
        val processInstance2 = mock<ProcessInstance>()
        val processInstanceId2 = "a69cf6c5-5e65-4dc9-81f6-2b64c12e3f0f"
        whenever(processInstance2.processInstanceId).thenReturn(processInstanceId2)

        val functionResult = mock<FunctionResult<ProcessDocumentInstance, OperationError>>()
        whenever(processDocumentAssociationService.getProcessDocumentInstanceResult(any()))
            .thenReturn(functionResult)

        whenever(functionResult.isError).thenReturn(false)
        val processDocumentInstance = mock<ProcessDocumentInstance>()
        whenever(functionResult.resultingValue()).thenReturn(Optional.of(processDocumentInstance))
        val pdiId = mock<ProcessDocumentInstanceId>()
        whenever(processDocumentInstance.processDocumentInstanceId()).thenReturn(pdiId)

        whenever(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(documentId.toString()).rootProcessInstances().list())
            .thenReturn(listOf(processInstance1, processInstance2))

        processDocumentDeletedEventListener!!.handle(DocumentDeletedEvent(documentId))

        verify(runtimeService).deleteProcessInstance("4320f9c0-5568-4ed2-91f9-d2c85fd4ce55", "Document deleted", false, true)
        verify(runtimeService).deleteProcessInstance("a69cf6c5-5e65-4dc9-81f6-2b64c12e3f0f", "Document deleted", false, true)
        verify(processDocumentAssociationService, times(2)).deleteProcessDocumentInstance(pdiId)
    }

    @Test
    fun `should delete process instances with business key when no document process instance exists`() {
        val documentId = UUID.fromString("d1f1b3ed-7575-45bb-a02b-18f378ddc34d")

        val processInstance1 = mock<ProcessInstance>()
        val processInstanceId1 = "4320f9c0-5568-4ed2-91f9-d2c85fd4ce55"
        whenever(processInstance1.processInstanceId).thenReturn(processInstanceId1)
        val processInstance2 = mock<ProcessInstance>()
        val processInstanceId2 = "a69cf6c5-5e65-4dc9-81f6-2b64c12e3f0f"
        whenever(processInstance2.processInstanceId).thenReturn(processInstanceId2)

        val functionResult = mock<FunctionResult<ProcessDocumentInstance, OperationError>>()
        whenever(processDocumentAssociationService.getProcessDocumentInstanceResult(any()))
            .thenReturn(functionResult)

        whenever(functionResult.isError).thenReturn(true)

        whenever(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(documentId.toString()).rootProcessInstances().list())
            .thenReturn(listOf(processInstance1, processInstance2))

        processDocumentDeletedEventListener!!.handle(DocumentDeletedEvent(documentId))

        verify(runtimeService).deleteProcessInstance("4320f9c0-5568-4ed2-91f9-d2c85fd4ce55", "Document deleted", false, true)
        verify(runtimeService).deleteProcessInstance("a69cf6c5-5e65-4dc9-81f6-2b64c12e3f0f", "Document deleted", false, true)
        verify(processDocumentAssociationService, never()).deleteProcessDocumentInstance(any())
    }
}