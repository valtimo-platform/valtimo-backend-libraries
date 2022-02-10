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
import org.assertj.core.api.Assertions
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Test
import java.util.UUID

internal class FixedValueResolverTest {

    private val fixedValueResolver = FixedValueResolver()

    @Test
    fun `should resolve boolean placeholder from placeholder`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()

        val resolvedValue = fixedValueResolver.resolveValue(
            placeholder = "true",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should resolve long placeholder from placeholder`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()

        val resolvedValue = fixedValueResolver.resolveValue(
            placeholder = "1337",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo(1337L)
    }

    @Test
    fun `should resolve double placeholder from placeholder`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()

        val resolvedValue = fixedValueResolver.resolveValue(
            placeholder = "13.37",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo(13.37)
    }

    @Test
    fun `should resolve string placeholder from placeholder`() {
        val processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString())
        val variableScope = DelegateTaskFake()

        val resolvedValue = fixedValueResolver.resolveValue(
            placeholder = "asdf",
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )

        Assertions.assertThat(resolvedValue).isEqualTo("asdf")
    }

}