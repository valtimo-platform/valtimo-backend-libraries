/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.BaseIntegrationTest
import java.util.UUID
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class CamundaHistoricVariableInstanceRepositoryIntTest @Autowired constructor(
    private val camundaHistoricVariableInstanceRepository: CamundaHistoricVariableInstanceRepository
): BaseIntegrationTest() {

    @Test
    @Transactional
    fun `should find camunda historic variable instance`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "one-task-process",
            UUID.randomUUID().toString(),
            mapOf("test" to true)
        )

        val result = camundaHistoricVariableInstanceRepository.findOne { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<String>("processInstanceId"),
                instance.id
            )
        }
        Assertions.assertThat(result.isPresent).isTrue()

        val historicVariableInstance = result.get()
        Assertions.assertThat(historicVariableInstance.processInstanceId).isEqualTo(instance.id)
    }
}