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
import com.ritense.formflow.domain.definition.FormFlowNextStep
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FormFlowInstanceTest {

    @Test
    fun `complete should return new step` () {
        val definition: FormFlowDefinition = mock()
        val steps: Set<FormFlowStep> = mutableSetOf(
            FormFlowStep(
                FormFlowStepId.create("test"),
                mutableListOf(FormFlowNextStep("123", "test2"))),
            FormFlowStep(
                FormFlowStepId.create("test2")
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        //complete task
        val result = instance.complete(instance.currentFormFlowStepInstanceId!!, "data")

        assertThat(instance.getHistory()[0].submissionData, equalTo("data"))
        assertNotNull(result)
        assertEquals(result!!.stepKey, "test2")
    }

    @Test
    fun `complete should return null when there are no next steps` () {
        val definition: FormFlowDefinition = mock()
        val steps: Set<FormFlowStep> = mutableSetOf(FormFlowStep(FormFlowStepId.create("test")))

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        //complete task
        val result = instance.complete(instance.currentFormFlowStepInstanceId!!, "data")

        assertThat(instance.getHistory()[0].submissionData, equalTo("data"))
        assertNull(result)
    }

    @Test
    fun `complete should throw exception when step in not current active step` () {
        val definition: FormFlowDefinition = mock()
        val steps: Set<FormFlowStep> = mutableSetOf(
            FormFlowStep(
                FormFlowStepId.create("test"),
                mutableListOf(FormFlowNextStep("123", "test2"))),
            FormFlowStep(
                FormFlowStepId.create("test2")
            )
        )

        whenever(definition.startStep).thenReturn("test")
        whenever(definition.steps).thenReturn(steps)

        val instance = FormFlowInstance(
            formFlowDefinition = definition
        )

        assertThrows<AssertionError> {
            instance.complete(FormFlowStepInstanceId.newId(), "data")
        }
    }
}