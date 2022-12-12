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

package com.ritense.formflow.service

import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class FormFlowServiceIntTest: BaseIntegrationTest() {
    @Autowired
    lateinit var formFlowService: FormFlowService

    @Autowired
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository

    @BeforeEach
    fun setUp() {
        formFlowInstanceRepository.deleteAll()
    }

    @Test
    fun `finds 2 formFlowInstances for 1 additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        assertEquals(2, formFlowService.findInstances(mutableMapOf("taskId" to "123")).size)
    }

    @Test
    fun `finds 1 formFlowInstance for 1 additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "1234")))
        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId" to "123")).size)
    }

//    TODO: Make this working for MySQL
//    @Test
//    fun `finds 1 formFlowInstance for 1 complex additionalProperty`() {
//        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
//        val henk = formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "123"))))
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "1234"))))
//        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "123"))).size)
//    }

//    TODO: Implement this as a feature
//    @Test
//    fun `finds 1 formFlowInstance for 1 complex additionalProperty with complex path`() {
//        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "123"))))
//        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to mutableMapOf("actualTaskId" to "1234"))))
//        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId.actualTaskId" to "123")).size)
//    }

    @Test
    fun `finds 0 formFlowInstances for 1 additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "123")))
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to "1234")))
        assertEquals(0, formFlowService.findInstances(mutableMapOf("documentId" to "123")).size)
    }

    @Test
    fun `finds 2 formFlowInstances for 2 additionalProperties`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(
            definition.createInstance(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "456",
                    "somethingId" to 123
                )
            )
        )
        formFlowService.save(
            definition.createInstance(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "456",
                    "somethingId" to 124
                )
            )
        )
        formFlowService.save(
            definition.createInstance(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "457",
                    "somethingId" to 123
                )
            )
        )
        assertEquals(
            2,
            formFlowService.findInstances(
                mutableMapOf(
                    "taskId" to "123",
                    "documentId" to "456"
                )
            ).size
        )
    }

    @Test
    fun `finds 1 formFlowInstance for 1 non-string additionalProperty`() {
        val definition = formFlowService.findLatestDefinitionByKey("inkomens_loket")!!
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to 123)))
        formFlowService.save(definition.createInstance(mutableMapOf("taskId" to 1234)))
        assertEquals(1, formFlowService.findInstances(mutableMapOf("taskId" to 123)).size)
    }
}
