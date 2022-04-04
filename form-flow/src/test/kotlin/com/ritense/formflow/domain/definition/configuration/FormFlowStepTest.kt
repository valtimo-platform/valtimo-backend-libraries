package com.ritense.formflow.domain.definition.configuration

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formflow.domain.definition.FormFlowStepId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FormFlowStepTest {

    @Test
    fun `contentEquals should match when nextsteps match with single entry`() {
        val otherMock: com.ritense.formflow.domain.definition.FormFlowNextStep = mock()

        val mockStep: FormFlowNextStep = mock()
        whenever(mockStep.contentEquals(otherMock)).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep
            )
        )
        val otherStep = com.ritense.formflow.domain.definition.FormFlowStep(
            FormFlowStepId("step-key"),
            mutableListOf(
                otherMock
            )
        )

        assertTrue(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should match when nextsteps match with multiple entries`() {
        val otherMock1: com.ritense.formflow.domain.definition.FormFlowNextStep = mock()
        val otherMock2: com.ritense.formflow.domain.definition.FormFlowNextStep = mock()

        val mockStep1: FormFlowNextStep = mock()
        whenever(mockStep1.contentEquals(otherMock1)).thenReturn(true)
        whenever(mockStep1.contentEquals(otherMock2)).thenReturn(false)

        val mockStep2: FormFlowNextStep = mock()
        whenever(mockStep2.contentEquals(otherMock1)).thenReturn(false)
        whenever(mockStep2.contentEquals(otherMock2)).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep1,
                mockStep2
            )
        )
        val otherStep = com.ritense.formflow.domain.definition.FormFlowStep(
            FormFlowStepId("step-key"),
            mutableListOf(
                otherMock1,
                otherMock2
            )
        )

        assertTrue(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when this step has more nextsteps then other`() {
        val mockStep1: FormFlowNextStep = mock()
        whenever(mockStep1.contentEquals(any())).thenReturn(true)

        val mockStep2: FormFlowNextStep = mock()
        whenever(mockStep2.contentEquals(any())).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep1,
                mockStep2
            )
        )
        val otherStep = com.ritense.formflow.domain.definition.FormFlowStep(
            FormFlowStepId("step-key"),
            mutableListOf(
                mock()
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when other step has more nextsteps then this`() {
        val mockStep: FormFlowNextStep = mock()
        whenever(mockStep.contentEquals(any())).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep
            )
        )
        val otherStep = com.ritense.formflow.domain.definition.FormFlowStep(
            FormFlowStepId("step-key"),
            mutableListOf(
                mock(),
                mock()
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when equal number of nextsteps but content is different`() {
        val otherMock1: com.ritense.formflow.domain.definition.FormFlowNextStep = mock()
        val otherMock2: com.ritense.formflow.domain.definition.FormFlowNextStep = mock()

        val mockStep1: FormFlowNextStep = mock()
        whenever(mockStep1.contentEquals(otherMock1)).thenReturn(true)
        whenever(mockStep1.contentEquals(otherMock2)).thenReturn(false)

        val mockStep2: FormFlowNextStep = mock()
        whenever(mockStep2.contentEquals(otherMock1)).thenReturn(false)
        whenever(mockStep2.contentEquals(otherMock2)).thenReturn(false)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep1,
                mockStep2
            )
        )
        val otherStep = com.ritense.formflow.domain.definition.FormFlowStep(
            FormFlowStepId("step-key"),
            mutableListOf(
                otherMock1,
                otherMock2
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when start step is different`() {
        val mockStep: FormFlowNextStep = mock()
        whenever(mockStep.contentEquals(any())).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep
            )
        )
        val otherStep = com.ritense.formflow.domain.definition.FormFlowStep(
            FormFlowStepId("other-key"),
            mutableListOf(
                mock()
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }
}