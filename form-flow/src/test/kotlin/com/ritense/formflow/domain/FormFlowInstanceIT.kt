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

import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class FormFlowInstanceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository

    @Test
    fun `create form flow instance successfully`() {
        val formFlowInstance = FormFlowInstance(
            formFlowDefinitionId = FormFlowDefinitionId.newId("test")
        )
        formFlowInstanceRepository.save(formFlowInstance)

        val storedInstance = formFlowInstanceRepository.findById(formFlowInstance.id).get()

        assertEquals(storedInstance, formFlowInstance)
    }

    @Test
    fun `update form flow instance successfully`() {
        val formFlowInstance = FormFlowInstance(
                formFlowDefinitionId = FormFlowDefinitionId.newId("test")
        )
        formFlowInstanceRepository.save(formFlowInstance)
        formFlowInstance.complete(formFlowInstance.currentFormFlowStepInstanceId!!, "{\"data\": \"data\"}")
        formFlowInstanceRepository.save(formFlowInstance)

        val storedInstance = formFlowInstanceRepository.findById(formFlowInstance.id).get()
        assertEquals(storedInstance.getHistory().size, 2)
        val firstStep = storedInstance.getHistory()[0]
        assertEquals(firstStep.submissionData, "{\"data\": \"data\"}")
        val secondStep = storedInstance.getHistory()[1]
        assertNull(secondStep.submissionData)
    }
}