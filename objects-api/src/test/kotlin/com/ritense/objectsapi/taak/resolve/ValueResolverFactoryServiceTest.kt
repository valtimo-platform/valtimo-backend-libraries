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

import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import java.util.UUID
import kotlin.jvm.Throws
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat

internal class ValueResolverFactoryServiceTest {

    private val resolverService = ValueResolverService(
        listOf( ProcessVariableValueResolverFactory(), FixedValueResolverFactory())
    )

    @Test
    fun `Should fail on duplicate resolver prefixes at init`() {
        val exception = assertThrows<RuntimeException> {
            ValueResolverService(
                listOf( ProcessVariableValueResolverFactory(), ProcessVariableValueResolverFactory())
            )
        }

        assertThat(exception.message).startsWith("Found more than 1 resolver for prefix 'pv'")
    }

    @Test
    fun `Should resolve list of requested values`() {
        val resolvedValues = resolverService.resolveValues(
            processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString()),
            variableScope = DelegateTaskFake()
                .withVariable("firstName", "John")
                .withVariable("lastName", "Doe")
                .withVariable("active", true),
            listOf(
                "pv:firstName",
                "pv:lastName",
                "pv:active",
                "fixedValue",
                "pv:nonexistant"
            )
        )

        assertThat(resolvedValues).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "pv:firstName" to "John",
                "pv:lastName" to "Doe",
                "pv:active" to true,
                "fixedValue" to "fixedValue",
            )
        )
    }

    @Test
    @Throws(RuntimeException::class)
    fun `Should throw exception on unknown prefix`() {
        val exception = assertThrows<RuntimeException> {
            resolverService.resolveValues(
                processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString()),
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
}