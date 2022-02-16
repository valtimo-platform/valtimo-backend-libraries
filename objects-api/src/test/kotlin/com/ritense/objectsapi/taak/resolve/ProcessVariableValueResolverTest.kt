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

package com.ritense.objectsapi.taak.resolve

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Test

internal class ProcessVariableValueResolverTest {
    private val runtimeService: RuntimeService = mock()
    private val processVariableValueResolver = ProcessVariableValueResolverFactory(runtimeService)

    @Test
    fun `should resolve requestedValue from process variables`() {
        val somePropertyName = "somePropertyName"
        val variableScope = DelegateTaskFake()
            .withVariable("firstName", "John")
            .withVariable(somePropertyName, true)
            .withVariable("lastName", "Doe")
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())

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
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())

        val resolvedValue = processVariableValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            somePropertyName
        )

        Assertions.assertThat(resolvedValue).isNull()
    }

    @Test
    fun `should handle value from process variables`() {
        val variableScope = DelegateTaskFake()
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())

        processVariableValueResolver.handleValues(
            processInstanceId, variableScope, mapOf("pv:firstName" to "John")
        )

        verify(runtimeService).setVariables(processInstanceId.toString(), mapOf("firstName" to "John"))
    }
}