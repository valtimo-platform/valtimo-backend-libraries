package com.ritense.formflow.domain

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FormFlowInstanceTest {

    @Test
    fun `complete should return new step id` () {
        val currentStep = FormFlowStepInstanceId.newId()
        val context = FormFlowInstanceContext(
            mutableListOf(),
            emptyMap()
        )
        val instance = FormFlowInstance(
            FormFlowInstanceId.newId(),
            FormFlowDefinitionId.newId("test"),
            context,
            currentStep
        )

        //add current step to history
        val historicalStep = FormFlowStepInstance(
            currentStep,
            instance,
            "current-step",
            1
        )
        context.history.add(historicalStep)

        //complete task
        var newInstanceId = instance.complete(currentStep, "data")

        assertThat(newInstanceId, instanceOf(FormFlowStepInstanceId::class.java))
        assertThat(context.history[0].submissionData, equalTo("data"))
    }

    @Test
    fun `complete should throw exception when step in not current active step` () {
        val instance = FormFlowInstance(
            FormFlowInstanceId.newId(),
            FormFlowDefinitionId.newId("test"),
            FormFlowInstanceContext(
                mutableListOf(),
                additionalProperties = emptyMap()
            ),
            FormFlowStepInstanceId.newId()
        )

        assertThrows<AssertionError> {
            instance.complete(FormFlowStepInstanceId.newId(), "data")
        }
    }
}