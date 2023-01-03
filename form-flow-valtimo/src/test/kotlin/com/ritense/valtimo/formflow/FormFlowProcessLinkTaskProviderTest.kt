package com.ritense.valtimo.formflow

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import org.camunda.bpm.engine.task.Task
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FormFlowProcessLinkTaskProviderTest {
    @Test
    fun `supports only bpmnElementFormFlowIdLink`() {
        val formLink1: BpmnElementFormFlowIdLink = mock()
        val formLink2: FormLink = mock()
        assertTrue(FormFlowProcessLinkTaskProvider(mock()).supports(formLink1))
        assertFalse(FormFlowProcessLinkTaskProvider(mock()).supports(formLink2))
    }
    @Test
    fun `getTaskResult contains formFlowId `() {
        val formLink: BpmnElementFormFlowIdLink = mock()
        val task: Task = mock()
        val formFlowService: FormFlowService = mock()
        val formFlowInstance: FormFlowInstance = mock()
        val formFlowInstanceId = FormFlowInstanceId.newId()

        whenever(formLink.formFlowId).thenReturn("123")
        whenever(formFlowService.findInstances(any())).thenReturn(listOf(formFlowInstance))
        whenever(formFlowInstance.id).thenReturn(formFlowInstanceId)

        val taskResult = FormFlowProcessLinkTaskProvider(formFlowService).getTaskResult(task, formLink)
        assertEquals("form-flow", taskResult.type)
        assertEquals(formFlowInstanceId.id, taskResult.properties.formFlowInstanceId)
    }
}