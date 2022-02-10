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
import org.assertj.core.api.Assertions
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Test

internal class ValueResolverFactoryServiceTest {

    private val resolverService = ValueResolverService(
        listOf( ProcessVariableValueResolverFactory(), FixedValueResolverFactory())
    )

    @Test
    fun `Should resolve list of placeholders`() {
        val resolvedPlaceholders = resolverService.resolvePlaceholders(
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

        Assertions.assertThat(resolvedPlaceholders).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "pv:firstName" to "John",
                "pv:lastName" to "Doe",
                "pv:active" to true,
                "fixedValue" to "fixedValue",
            )
        )
    }
}