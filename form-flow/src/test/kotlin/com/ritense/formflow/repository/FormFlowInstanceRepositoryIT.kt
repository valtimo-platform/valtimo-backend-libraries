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

package com.ritense.formflow.repository

import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.domain.FormFlowDefinitionId
import com.ritense.formflow.domain.FormFlowInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FormFlowInstanceRepositoryIT : BaseIntegrationTest() {

    @Test
    fun `create form flow instance successfully`() {
        val formFlowInstance = FormFlowInstance(
            formFlowDefinitionId = FormFlowDefinitionId.newId("test")
        )
        val savedFormFlowInstance = formFlowInstance.save()

        assertThat(savedFormFlowInstance == formFlowInstance)
    }

    @Test
    fun `update form flow instance successfully`() {
        val formFlowInstance = FormFlowInstance(
                formFlowDefinitionId = FormFlowDefinitionId.newId("test")).save()

        formFlowInstance.complete(formFlowInstance.currentFormFlowStepInstanceId!!, "something")


    }

    @Test
    fun `delete form flow instance successfully`() {

    }
}