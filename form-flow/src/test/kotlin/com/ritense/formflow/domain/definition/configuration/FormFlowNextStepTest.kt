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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import com.ritense.formflow.domain.definition.FormFlowNextStep as FormFlowNextStepEnitity

class FormFlowNextStepTest {
    @Test
    fun `contentEquals should match when contents are identical`() {
        val thisNextStep = FormFlowNextStep("condition1", "step1")
        val otherNextStep = FormFlowNextStepEnitity("condition1", "step1")

        assertTrue(thisNextStep.contentEquals(otherNextStep))
    }

    @Test
    fun `contentEquals should not match when conditions differ`() {
        val thisNextStep = FormFlowNextStep("condition1", "step1")
        val otherNextStep = FormFlowNextStepEnitity("condition2", "step1")

        assertFalse(thisNextStep.contentEquals(otherNextStep))
    }

    @Test
    fun `contentEquals should not match when steps differ`() {
        val thisNextStep = FormFlowNextStep("condition1", "step1")
        val otherNextStep = FormFlowNextStepEnitity("condition1", "step2")

        assertFalse(thisNextStep.contentEquals(otherNextStep))
    }
}