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

package com.ritense.formflow.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowNextStep
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.configuration.FormFlowStepType
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FormFlowInstanceTest {
    @Test
    fun `complete should return new step`() {
        val instance = FormFlowInstance(
            formFlowDefinition = FormFlowDefinition(
                id = FormFlowDefinitionId("test", 1L),
                startStep = "test",
                steps = mutableSetOf(
                    FormFlowStep(
                        id = FormFlowStepId.create("test"),
                        nextSteps = mutableListOf(FormFlowNextStep("123", "test2")),
                        type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
                    ),
                    FormFlowStep(
                        id = FormFlowStepId.create("test2"),
                        type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
                    )
                )
            )
        )

        //complete task
        val result = instance.complete(instance.currentFormFlowStepInstanceId!!, JSONObject("{\"data\":\"data\"}"))

        assertThat(instance.getHistory()[0].submissionData, equalTo("{\"data\":\"data\"}"))
        assertNotNull(result)
        assertEquals("test2", result!!.stepKey)
    }

    @Test
    fun `complete should return null when there are no next steps`() {
        val instance = FormFlowInstance(
            formFlowDefinition = FormFlowDefinition(
                id = FormFlowDefinitionId("test", 1L),
                startStep = "test",
                steps = mutableSetOf(
                    FormFlowStep(
                        id = FormFlowStepId.create("test"),
                        type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
                    )
                )
            )
        )

        //complete task
        val result = instance.complete(instance.currentFormFlowStepInstanceId!!, JSONObject("{\"data\":\"data\"}"))

        assertThat(instance.getHistory()[0].submissionData, equalTo("{\"data\":\"data\"}"))
        assertNull(result)
    }

    @Test
    fun `complete should throw exception when step in not current active step`() {
        val definition: FormFlowDefinition = mock()
        val steps: Set<FormFlowStep> = mutableSetOf(
            FormFlowStep(
                id = FormFlowStepId.create("test"),
                nextSteps = mutableListOf(
                    FormFlowNextStep("123", "test2")
                ),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            ),
            FormFlowStep(
                id = FormFlowStepId.create("test2"),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        assertThrows<AssertionError> {
            instance.complete(FormFlowStepInstanceId.newId(), JSONObject("{\"data\": \"data\"}"))
        }
    }

    @Test
    fun `getSubmissionDataContext should be empty if no steps have been completed`() {
        val definition: FormFlowDefinition = mock()
        val steps: Set<FormFlowStep> = mutableSetOf(
            FormFlowStep(
                id = FormFlowStepId.create("test"),
                nextSteps = mutableListOf(FormFlowNextStep("123", "test2")),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            ),
            FormFlowStep(
                id = FormFlowStepId.create("test2"),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        val submissionDataContext = instance.getSubmissionDataContext()

        assertEquals("{}", submissionDataContext)
    }

    @Test
    fun `getSubmissionDataContext should not be empty if one step with submission data has been completed`() {
        val definition: FormFlowDefinition = mock()
        val step1 = FormFlowStep(
            id = FormFlowStepId.create("test"),
            nextSteps = mutableListOf(FormFlowNextStep("123", "test2")),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )

        val steps: Set<FormFlowStep> = mutableSetOf(
            step1,
            FormFlowStep(
                id = FormFlowStepId.create("test2"),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)
        whenever(definition.getStepByKey("test")).thenReturn(step1)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        instance.complete(instance.currentFormFlowStepInstanceId!!, JSONObject("{\"data\":\"data\"}"))

        val submissionDataContext = instance.getSubmissionDataContext()

        assertEquals("{\"data\":\"data\"}", submissionDataContext)
    }

    @Test
    fun `getSubmissionDataContext should overwrite submissionData from a previous steps`() {
        val definition: FormFlowDefinition = mock()
        val step1 = FormFlowStep(
            id = FormFlowStepId.create("test"),
            nextSteps = mutableListOf(FormFlowNextStep("123", "test2")),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )

        val step2 = FormFlowStep(
            id = FormFlowStepId.create("test2"),
            nextSteps = mutableListOf(FormFlowNextStep("123", "test3")),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )

        val steps: Set<FormFlowStep> = mutableSetOf(
            step1,
            step2,
            FormFlowStep(
                id = FormFlowStepId.create("test3"),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)
        whenever(definition.getStepByKey("test")).thenReturn(step1)
        whenever(definition.getStepByKey("test2")).thenReturn(step2)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        instance.complete(instance.currentFormFlowStepInstanceId!!, JSONObject("{\"data\":\"data\"}"))
        instance.complete(instance.currentFormFlowStepInstanceId!!, JSONObject("{\"data\":\"data2\"}"))

        val submissionDataContext = instance.getSubmissionDataContext()

        assertEquals("{\"data\":\"data2\"}", submissionDataContext)
    }

    @Test
    fun `getSubmissionDataContext should append submissionData from a previous steps`() {
        val definition: FormFlowDefinition = mock()
        val step1 = FormFlowStep(
            id = FormFlowStepId.create("test"),
            nextSteps = mutableListOf(FormFlowNextStep("123", "test2")),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )

        val step2 = FormFlowStep(
            id = FormFlowStepId.create("test2"),
            nextSteps = mutableListOf(FormFlowNextStep("123", "test3")),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )

        val steps: Set<FormFlowStep> = mutableSetOf(
            step1,
            step2,
            FormFlowStep(
                id = FormFlowStepId.create("test3"),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)
        whenever(definition.getStepByKey("test")).thenReturn(step1)
        whenever(definition.getStepByKey("test2")).thenReturn(step2)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        instance.complete(instance.currentFormFlowStepInstanceId!!, JSONObject("{\"data\":\"data\"}"))
        instance.complete(instance.currentFormFlowStepInstanceId!!, JSONObject("{\"data2\":\"data2\"}"))

        val submissionDataContext = instance.getSubmissionDataContext()

        assertEquals("{\"data\":\"data\",\"data2\":\"data2\"}", submissionDataContext)
    }

    @Test
    fun `getSubmissionDataContext should append nested submissionData from a previous steps`() {
        val definition: FormFlowDefinition = mock()
        val step1 = FormFlowStep(
            id = FormFlowStepId.create("test"),
            nextSteps = mutableListOf(FormFlowNextStep("123", "test2")),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )

        val step2 = FormFlowStep(
            id = FormFlowStepId.create("test2"),
            nextSteps = mutableListOf(FormFlowNextStep("123", "test3")),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )

        val steps: Set<FormFlowStep> = mutableSetOf(
            step1,
            step2,
            FormFlowStep(
                id = FormFlowStepId.create("test3"),
                type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)
        whenever(definition.getStepByKey("test")).thenReturn(step1)
        whenever(definition.getStepByKey("test2")).thenReturn(step2)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        instance.complete(
            instance.currentFormFlowStepInstanceId!!,
            JSONObject("{\"data\":{\"data\":\"data\",\"data2\":\"data\"}}")
        )
        instance.complete(
            instance.currentFormFlowStepInstanceId!!,
            JSONObject("{\"data\":{\"data\":\"data2\"}}")
        )

        val submissionDataContext = instance.getSubmissionDataContext()

        assertEquals("{\"data\":{\"data\":\"data2\",\"data2\":\"data\"}}", submissionDataContext)
    }
}