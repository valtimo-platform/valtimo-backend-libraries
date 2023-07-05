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

package com.ritense.valtimo.camunda.service

import com.ritense.valtimo.camunda.domain.CamundaVariableInstance
import com.ritense.valtimo.camunda.repository.CamundaVariableInstanceRepository
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals

class CamundaRuntimeServiceTest {

    val runtimeService: RuntimeService = mock()
    val camundaVariableInstanceRepository: CamundaVariableInstanceRepository = mock()

    @Test
    fun `should get variables`() {
        val camundaRuntimeService = CamundaRuntimeService(
            runtimeService,
            camundaVariableInstanceRepository
        )
        val variableInstances = listOf(
            createMockedVariableInstance("val1", "nothing"),
            createMockedVariableInstance("val2", "something")
        )
        whenever(camundaVariableInstanceRepository.findAll(any(), any<Sort>())).thenReturn(variableInstances)
        val processInstanceVariables = camundaRuntimeService.getVariables("123", listOf("val1", "val2"))
        assertEquals(2, processInstanceVariables.size)
        assertEquals("nothing", processInstanceVariables["val1"])
        assertEquals("something", processInstanceVariables["val2"])
    }

    @Test
    fun `should get process variables with empty values`() {
        val camundaRuntimeService = CamundaRuntimeService(
            runtimeService,
            camundaVariableInstanceRepository
        )
        val variableInstances = listOf(
            createMockedVariableInstance("val1", null),
            createMockedVariableInstance("val2", "something")
        )
        whenever(camundaVariableInstanceRepository.findAll(any(), any<Sort>())).thenReturn(variableInstances)
        val processInstanceVariables = camundaRuntimeService.getVariables("123", listOf("val1", "val2"))
        assertEquals(1, processInstanceVariables.size)
        assertEquals("something", processInstanceVariables["val2"])
    }

    private fun createMockedVariableInstance(name: String, value: Any?): CamundaVariableInstance {
        val instance = mock<CamundaVariableInstance>()
        whenever(instance.name).thenReturn(name)
        whenever(instance.getValue()).thenReturn(value)
        return instance
    }
}