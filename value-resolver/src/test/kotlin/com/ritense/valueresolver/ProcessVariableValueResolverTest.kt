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

package com.ritense.valueresolver

import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.mockito.delegate.DelegateCaseVariableInstanceFake
import org.camunda.community.mockito.delegate.DelegateTaskFake
import org.camunda.community.mockito.process.ProcessInstanceFake
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

internal class ProcessVariableValueResolverTest {
    private val runtimeService: RuntimeService = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val processVariableValueResolver = ProcessVariableValueResolverFactory(runtimeService)

    @Test
    fun `should resolve requestedValue from process variables`() {
        val somePropertyName = "somePropertyName"
        val variableScope = DelegateTaskFake()
            .withVariable("firstName", "John")
            .withVariable(somePropertyName, true)
            .withVariable("lastName", "Doe")
        val processInstanceId = UUID.randomUUID().toString()

        val resolvedValue = processVariableValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            somePropertyName
        )

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should NOT resolve requestedValue from process variables`() {
        val somePropertyName = "somePropertyName"
        val variableScope = DelegateTaskFake()
            .withVariable("firstName", "John")
            .withVariable("lastName", "Doe")
        val processInstanceId = UUID.randomUUID().toString()

        val resolvedValue = processVariableValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            somePropertyName
        )

        Assertions.assertThat(resolvedValue).isNull()
    }

    @Test
    fun `should resolve requestedValue from process variables by document ID`() {
        val somePropertyName = "somePropertyName"
        val documentInstanceId = UUID.randomUUID().toString()
        val processInstance = ProcessInstanceFake.builder().processInstanceId(UUID.randomUUID().toString()).build()
        whenever(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(documentInstanceId).list())
            .thenReturn(listOf(processInstance))
        val variableInstance = DelegateCaseVariableInstanceFake().create(somePropertyName, Variables.booleanValue(true))
        whenever(runtimeService.createVariableInstanceQuery()
            .processInstanceIdIn(processInstance.id)
            .variableName(somePropertyName)
            .list())
            .thenReturn(listOf(variableInstance))

        val resolvedValue = processVariableValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            somePropertyName
        )

        Assertions.assertThat(resolvedValue).isEqualTo(listOf(true))
    }

    @Test
    fun `should handle value from process variables`() {
        val variableScope = DelegateTaskFake()
        val processInstanceId = UUID.randomUUID().toString()

        processVariableValueResolver.handleValues(
            processInstanceId, variableScope, mapOf("firstName" to "John")
        )

        verify(runtimeService).setVariables(processInstanceId, mapOf("firstName" to "John"))
    }
}
