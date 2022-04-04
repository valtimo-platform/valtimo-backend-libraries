package com.ritense.formflow.domain.configuration

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.configuration.FormFlowDefinition
import com.ritense.formflow.domain.definition.configuration.FormFlowStep
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormFlowDefinitionTest {
    @Test
    fun `contentEquals should match when steps match with single entry`() {
        val thisStep: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep))
        val otherStep = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key2"))
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep))

        whenever(thisStep.contentEquals(otherStep)).thenReturn(true)

        assertTrue(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should match when steps match with multiple entries`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherStep1 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key2"))
        val otherStep2 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key3"))
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2))

        whenever(thisStep1.contentEquals(otherStep1)).thenReturn(true)
        whenever(thisStep2.contentEquals(otherStep2)).thenReturn(true)

        assertTrue(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should match when no steps`() {
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf())
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf())

        assertTrue(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when this has more steps then other`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherStep1 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key2"))
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1))

        whenever(thisStep1.contentEquals(otherStep1)).thenReturn(true)

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when other has more steps then this`() {
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep2))
        val otherStep1 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key2"))
        val otherStep2 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key3"))
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2))

        whenever(thisStep2.contentEquals(otherStep2)).thenReturn(true)

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when this steps are empty`() {
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf())
        val otherStep1 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key2"))
        val otherStep2 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key3"))
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2))

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when other steps are empty`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf())

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when equal number of steps but content is different`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherStep1 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key2"))
        val otherStep2 = com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId("key3"))
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2))

        whenever(thisStep1.contentEquals(otherStep1)).thenReturn(true)
        whenever(thisStep2.contentEquals(otherStep2)).thenReturn(false)

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when start step is different`() {
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf())
        val otherDefinition = com.ritense.formflow.domain.definition.FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "test2", mutableSetOf())

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }
}