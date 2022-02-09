package com.ritense.objectsapi.taak.resolve

import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.extension.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.Test

internal class ProcessVariableValueResolverTest {
    private val processVariableValueResolver = ProcessVariableValueResolver()

    @Test
    fun `should resolve placeholder from process variables`() {
        val somePropertyName = "somePropertyName"
        val resolvedValue = processVariableValueResolver.resolveValue(
            placeholder = "pv:$somePropertyName",
            processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString()),
            variableScope = DelegateTaskFake()
                .withVariable("firstName", "John")
                .withVariable(somePropertyName, true)
                .withVariable("lastName", "Doe")
        )

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should NOT resolve placeholder from process variables`() {
        val somePropertyName = "somePropertyName"
        val resolvedValue = processVariableValueResolver.resolveValue(
            placeholder = "pv:$somePropertyName",
            processInstanceId = CamundaProcessInstanceId(UUID.randomUUID().toString()),
            variableScope = DelegateTaskFake()
                .withVariable("firstName", "John")
                .withVariable("lastName", "Doe")
        )

        Assertions.assertThat(resolvedValue).isNull()
    }
}