package com.ritense.valtimo.formflow

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formlink.domain.FormLink
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FormFlowProcessLinkTaskProviderTest {
    @Test
    fun `supports only bpmnElementFormFlowIdLink`() {
        val formLink1: BpmnElementFormFlowIdLink = mock()
        val formLink2: FormLink = mock()
        assertTrue(FormFlowProcessLinkTaskProvider().supports(formLink1))
        assertFalse(FormFlowProcessLinkTaskProvider().supports(formLink2))
    }
    @Test
    fun `getTaskResult contains formFlowId `() {
        val formLink: BpmnElementFormFlowIdLink = mock()
        whenever(formLink.formFlowId).thenReturn("123")
        val taskResult = FormFlowProcessLinkTaskProvider().getTaskResult(formLink)
        assertEquals("form-flow", taskResult.type)
        assertEquals("123", taskResult.properties.formFlowLinkId)
    }
}