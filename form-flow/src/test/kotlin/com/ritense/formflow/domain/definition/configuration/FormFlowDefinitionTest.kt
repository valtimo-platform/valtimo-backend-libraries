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

package com.ritense.formflow.domain.definition.configuration

import com.ritense.formflow.domain.definition.FormFlowDefinition as FormFlowDefinitionEntity
import com.ritense.formflow.domain.definition.FormFlowStep as FormFlowStepEntity
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FormFlowDefinitionTest {
    @Test
    fun `contentEquals should match when steps match with single entry`() {
        val thisStep: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep))
        val otherStep = FormFlowStepEntity(
            FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep)
        )

        whenever(thisStep.contentEquals(otherStep)).thenReturn(true)

        assertTrue(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should match when steps match with multiple entries`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherStep1 = FormFlowStepEntity(
            FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherStep2 = FormFlowStepEntity(
            FormFlowStepId("key3"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2)
        )

        whenever(thisStep1.contentEquals(otherStep1)).thenReturn(true)
        whenever(thisStep2.contentEquals(otherStep2)).thenReturn(true)

        assertTrue(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should match when no steps`() {
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf())
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf()
        )

        assertTrue(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when this has more steps then other`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherStep1 = FormFlowStepEntity(
            FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1)
        )

        whenever(thisStep1.contentEquals(otherStep1)).thenReturn(true)
        whenever(thisStep2.contentEquals(otherStep1)).thenReturn(true)

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when other has more steps then this`() {
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep2))
        val otherStep1 = FormFlowStepEntity(
            FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherStep2 = FormFlowStepEntity(
            FormFlowStepId("key3"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2)
        )

        whenever(thisStep2.contentEquals(otherStep2)).thenReturn(true)

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when this steps are empty`() {
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf())
        val otherStep1 = FormFlowStepEntity(
            FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherStep2 = FormFlowStepEntity(
            FormFlowStepId("key3"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2)
        )

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when other steps are empty`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf()
        )

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when equal number of steps but content is different`() {
        val thisStep1: FormFlowStep = mock()
        val thisStep2: FormFlowStep = mock()
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf(thisStep1, thisStep2))
        val otherStep1 = FormFlowStepEntity(
            FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherStep2 = FormFlowStepEntity(
            FormFlowStepId("key3"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test1", mutableSetOf(otherStep1, otherStep2)
        )

        whenever(thisStep1.contentEquals(otherStep1)).thenReturn(true)
        whenever(thisStep2.contentEquals(otherStep2)).thenReturn(false)

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }

    @Test
    fun `contentEquals should not match when start step is different`() {
        val thisDefinition = FormFlowDefinition("test1", mutableSetOf())
        val otherDefinition = FormFlowDefinitionEntity(
            id = FormFlowDefinitionId.newId("key1"), "test2", mutableSetOf()
        )

        assertFalse(thisDefinition.contentEquals(otherDefinition))
    }
}