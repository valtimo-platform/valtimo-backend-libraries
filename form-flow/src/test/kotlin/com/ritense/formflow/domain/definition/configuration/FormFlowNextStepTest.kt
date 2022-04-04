package com.ritense.formflow.domain.definition.configuration

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormFlowNextStepTest {
    @Test
    fun `contentEquals should match when contents are identical`() {
        val thisNextStep = FormFlowNextStep("condition1", "step1")
        val otherNextStep = com.ritense.formflow.domain.definition.FormFlowNextStep("condition1", "step1")

        assertTrue(thisNextStep.contentEquals(otherNextStep))
    }

    @Test
    fun `contentEquals should not match when conditions differ`() {
        val thisNextStep = FormFlowNextStep("condition1", "step1")
        val otherNextStep = com.ritense.formflow.domain.definition.FormFlowNextStep("condition2", "step1")

        assertFalse(thisNextStep.contentEquals(otherNextStep))
    }

    @Test
    fun `contentEquals should not match when steps differ`() {
        val thisNextStep = FormFlowNextStep("condition1", "step1")
        val otherNextStep = com.ritense.formflow.domain.definition.FormFlowNextStep("condition1", "step2")

        assertFalse(thisNextStep.contentEquals(otherNextStep))
    }
}