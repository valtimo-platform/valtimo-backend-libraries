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

import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FormFlowInstanceTest {

    @Test
    fun `complete should return new step id` () {
        val instance = FormFlowInstance(
            formFlowDefinitionId = FormFlowDefinitionId.newId("test")
        )

        //complete task
        instance.complete(instance.currentFormFlowStepInstanceId!!, "data")

        assertThat(instance.getHistory()[0].submissionData, equalTo("data"))
    }

    @Test
    fun `complete should throw exception when step in not current active step` () {
        val instance = FormFlowInstance(
            formFlowDefinitionId = FormFlowDefinitionId.newId("test")
        )

        assertThrows<AssertionError> {
            instance.complete(FormFlowStepInstanceId.newId(), "data")
        }
    }
}