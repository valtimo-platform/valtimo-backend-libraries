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

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.UUID

internal class ValueResolverFactoryServiceImplTest {

    private val runtimeService: RuntimeService = mock()
    private val resolverService = ValueResolverServiceImpl(
        listOf(ProcessVariableValueResolverFactory(runtimeService), FixedValueResolverFactory())
    )

    @Test
    fun `Should fail on duplicate resolver prefixes`() {
        val exception = assertThrows<RuntimeException> {
            val resolverService = ValueResolverServiceImpl(
                listOf(
                    ProcessVariableValueResolverFactory(runtimeService),
                    ProcessVariableValueResolverFactory(runtimeService)
                )
            )

            resolverService.resolveValues(
                processInstanceId = UUID.randomUUID().toString(),
                variableScope = DelegateTaskFake(),
                listOf(
                    "pv:dummy"
                )
            )
        }

        assertThat(exception.message).startsWith("Expected 1 resolver for prefix 'pv'")
    }

    @Test
    fun `Should resolve list of requested values`() {
        val resolvedValues = resolverService.resolveValues(
            processInstanceId = UUID.randomUUID().toString(),
            variableScope = DelegateTaskFake()
                .withVariable("firstName", "John")
                .withVariable("lastName", "Doe")
                .withVariable("active", true)
                .withVariable("nullValue", null),
            listOf(
                "pv:firstName",
                "pv:lastName",
                "pv:active",
                "pv:nullValue",
                "fixedValue",
                "pv:nonexistant"
            )
        )

        assertThat(resolvedValues).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "pv:firstName" to "John",
                "pv:lastName" to "Doe",
                "pv:active" to true,
                "pv:nullValue" to null,
                "fixedValue" to "fixedValue",
                "pv:nonexistant" to null,
            )
        )
    }

    @Test
    @Throws(RuntimeException::class)
    fun `Should throw exception on unknown prefix`() {
        val exception = assertThrows<RuntimeException> {
            resolverService.resolveValues(
                processInstanceId = UUID.randomUUID().toString(),
                variableScope = DelegateTaskFake()
                    .withVariable("firstName", "John")
                    .withVariable("lastName", "Doe")
                    .withVariable("active", true),
                listOf(
                    "xyz:firstName"
                )
            )
        }

        assertThat(exception.message).startsWith("No resolver factory found for value prefix xyz")
    }

    @Test
    @Throws(RuntimeException::class)
    fun `Should throw exception when no resolvers are configured`() {
        val resolverService = ValueResolverServiceImpl(listOf())
        val exception = assertThrows<RuntimeException> {
            resolverService.resolveValues(
                processInstanceId = UUID.randomUUID().toString(),
                variableScope = DelegateTaskFake()
                    .withVariable("firstName", "John")
                    .withVariable("lastName", "Doe")
                    .withVariable("active", true),
                listOf(
                    "xyz:firstName"
                )
            )
        }

        assertThat(exception.message).startsWith("No resolver factory found for value prefix xyz")
    }

    @Test
    fun `Should handle list of values`() {
        val processInstanceId = UUID.randomUUID().toString()
        val variableScope = DelegateTaskFake()

        resolverService.handleValues(
            processInstanceId, variableScope, mapOf(
                "pv:firstName" to "John",
                "pv:lastName" to "Doe",
                "pv:active" to true,
            )
        )

        verify(runtimeService).setVariables(processInstanceId, mapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "active" to true,
        ))
    }
}