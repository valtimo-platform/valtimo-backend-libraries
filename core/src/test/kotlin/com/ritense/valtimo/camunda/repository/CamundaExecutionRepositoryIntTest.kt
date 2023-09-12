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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class CamundaExecutionRepositoryIntTest @Autowired constructor(
    private val camundaExecutionRepository: CamundaExecutionRepository
): BaseIntegrationTest() {

    @Test
    @Transactional
    fun `should find a camunda execution`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "one-task-process",
            UUID.randomUUID().toString(),
            mapOf("test" to true)
        )

        val result = camundaExecutionRepository.findById(instance.id)
        assertThat(result.isPresent).isTrue()

        val execution = result.get()
        assertThat(execution.id).isEqualTo(instance.id)
        assertThat(execution.businessKey).isEqualTo(instance.businessKey)
        val variable = execution.variableInstances.first { it.name == "test" }
        assertThat(variable.longValue).isEqualTo(1L)
    }
}