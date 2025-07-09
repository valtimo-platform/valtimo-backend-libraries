/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.operaton.service

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.valtimo.operaton.domain.OperatonVariableInstance
import com.ritense.valtimo.operaton.repository.OperatonIdentityLinkRepository
import com.ritense.valtimo.operaton.repository.OperatonVariableInstanceRepository
import com.ritense.valtimo.contract.json.MapperSingleton
import org.operaton.bpm.engine.RuntimeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals

class OperatonRuntimeServiceTest {

    val runtimeService: RuntimeService = mock()
    val operatonVariableInstanceRepository: OperatonVariableInstanceRepository = mock()
    val operatonIdentityLinkRepository: OperatonIdentityLinkRepository = mock()
    val authorizationService: AuthorizationService = mock()
    lateinit var operatonRuntimeService: OperatonRuntimeService

    @BeforeEach
    fun beforeEach() {
        operatonRuntimeService = OperatonRuntimeService(
            runtimeService,
            operatonVariableInstanceRepository,
            operatonIdentityLinkRepository,
            authorizationService,
            MapperSingleton.get(),
        )
    }

    @Test
    fun `should get variables`() {
        val variableInstances = listOf(
            createMockedVariableInstance("val1", "nothing"),
            createMockedVariableInstance("val2", "something")
        )
        whenever(operatonVariableInstanceRepository.findAll(any(), any<Sort>())).thenReturn(variableInstances)
        val processInstanceVariables = runWithoutAuthorization {
            operatonRuntimeService.getVariables("123", listOf("val1", "val2"))
        }
        assertEquals(2, processInstanceVariables.size)
        assertEquals("nothing", processInstanceVariables["val1"])
        assertEquals("something", processInstanceVariables["val2"])
    }

    @Test
    fun `should get process variables with empty values`() {
        val variableInstances = listOf(
            createMockedVariableInstance("val1", null),
            createMockedVariableInstance("val2", "something")
        )
        whenever(operatonVariableInstanceRepository.findAll(any(), any<Sort>())).thenReturn(variableInstances)
        val processInstanceVariables = runWithoutAuthorization {
            operatonRuntimeService.getVariables("123", listOf("val1", "val2"))
        }
        assertEquals(1, processInstanceVariables.size)
        assertEquals("something", processInstanceVariables["val2"])
    }

    @Test
    fun `should get process variables with jsonPointer`() {
        val variableInstances = listOf(
            createMockedVariableInstance("person", mapOf("firstName" to "John", "lastName" to "Doe")),
        )
        whenever(operatonVariableInstanceRepository.findAll(any(), any<Sort>())).thenReturn(variableInstances)
        val result = runWithoutAuthorization {
            operatonRuntimeService.getVariablesByJsonPointers("123", listOf(
                JsonPointer.valueOf("/person/firstName"),
                JsonPointer.valueOf("/person/lastName"),
            ))
        }
        assertEquals(2, result.size)
        assertEquals("John", result["/person/firstName"])
        assertEquals("Doe", result["/person/lastName"])
    }

    private fun createMockedVariableInstance(name: String, value: Any?): OperatonVariableInstance {
        val instance = mock<OperatonVariableInstance>()
        whenever(instance.name).thenReturn(name)
        whenever(instance.getValue()).thenReturn(value)
        return instance
    }
}