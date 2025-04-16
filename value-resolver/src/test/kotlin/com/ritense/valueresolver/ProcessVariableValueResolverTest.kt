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

package com.ritense.valueresolver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.valtimo.contract.json.MapperSingleton
import java.time.LocalDate
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.bpm.engine.variable.impl.value.builder.SerializedObjectValueBuilderImpl
import org.camunda.community.mockito.delegate.DelegateCaseVariableInstanceFake
import org.camunda.community.mockito.delegate.DelegateTaskFake
import org.camunda.community.mockito.process.ProcessInstanceFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ProcessVariableValueResolverTest {
    private val runtimeService: RuntimeService = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val objectMapper = MapperSingleton.get()
    private val processVariableValueResolver = ProcessVariableValueResolverFactory(runtimeService, objectMapper)

    @BeforeEach
    fun setUp() {
        val commandContext: CommandContext = mock()
        whenever(commandContext.variableInstanceManager).thenReturn(mock())
        Context.setCommandContext(commandContext)
    }

    @Test
    fun `should resolve requestedValue from process variables`() {
        val somePropertyName = "somePropertyName"
        val now = LocalDate.now()
        val variableScope = DelegateTaskFake()
            .withVariable("firstName", "John")
            .withVariable(somePropertyName, true)
            .withVariable("lastName", "Doe")
            .withVariable("dateTime", now)
        val processInstanceId = UUID.randomUUID().toString()

        val resolver = processVariableValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )
        val somePropertyValue = resolver.apply(somePropertyName)
        val serializedValue = resolver.apply("dateTime")

        Assertions.assertThat(somePropertyValue).isEqualTo(true)
        Assertions.assertThat(serializedValue).isEqualTo(now)
    }

    @Test
    fun `should resolve legacy requestedValue with dots from process variables`() {
        val variableScope = DelegateTaskFake()
            .withVariable("person.firstName", "John")

        val resolver = processVariableValueResolver.createResolver(
            processInstanceId = UUID.randomUUID().toString(),
            variableScope = variableScope
        )

        Assertions.assertThat(resolver.apply("person.firstName")).isEqualTo("John")
    }

    @Test
    fun `should resolve json from process variables`() {
        val personVariable = objectMapper.readValue<JsonNode>("""
                {
                    "firstName":"John",
                    "birthDate":"2000-01-01",
                    "verified": true,
                    "location":{
                        "streetName":"Funenpark",
                        "streetNumber": 1
                    }
                }
            """)
        val variableScope = DelegateTaskFake()
            .withVariable("person", personVariable)

        val resolver = processVariableValueResolver.createResolver(
            processInstanceId =  UUID.randomUUID().toString(),
            variableScope = variableScope
        )

        Assertions.assertThat(resolver.apply("/person/firstName")).isEqualTo("John")
        Assertions.assertThat(resolver.apply("/person/birthDate")).isEqualTo("2000-01-01")
        Assertions.assertThat(resolver.apply("/person/verified")).isEqualTo(true)
        Assertions.assertThat(resolver.apply("/person/location/streetName")).isEqualTo("Funenpark")
        Assertions.assertThat(resolver.apply("/person/location/streetNumber")).isEqualTo(1)
        Assertions.assertThat(resolver.apply("/person/location")).isEqualTo(mapOf("streetName" to "Funenpark", "streetNumber" to 1))
        Assertions.assertThat(resolver.apply("/person")).isEqualTo(objectMapper.convertValue<Map<String, Any?>>(personVariable))
        Assertions.assertThat(resolver.apply("person.firstName")).isEqualTo("John")
        Assertions.assertThat(resolver.apply("person.birthDate")).isEqualTo("2000-01-01")
        Assertions.assertThat(resolver.apply("person.verified")).isEqualTo(true)
        Assertions.assertThat(resolver.apply("person.location.streetName")).isEqualTo("Funenpark")
        Assertions.assertThat(resolver.apply("person.location.streetNumber")).isEqualTo(1)
        Assertions.assertThat(resolver.apply("person.location")).isEqualTo(mapOf("streetName" to "Funenpark", "streetNumber" to 1))
        Assertions.assertThat(resolver.apply("person")).isEqualTo(personVariable)
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

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should resolve json from process variables by document ID`() {
        val personVariable = objectMapper.readValue<JsonNode>("""
                {
                    "firstName":"John",
                    "birthDate":"2000-01-01",
                    "verified": true,
                    "location":{
                        "streetName":"Funenpark",
                        "streetNumber": 1
                    }
                }
            """)
        val documentInstanceId = UUID.randomUUID().toString()
        val processInstance = ProcessInstanceFake.builder().processInstanceId(UUID.randomUUID().toString()).build()
        whenever(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(documentInstanceId).list())
            .thenReturn(listOf(processInstance))
        val variableInstance = DelegateCaseVariableInstanceFake().create(
            "person",
            SerializedObjectValueBuilderImpl(ObjectValueImpl(personVariable)).create()
        )
        whenever(runtimeService.createVariableInstanceQuery()
            .processInstanceIdIn(processInstance.id)
            .variableName("person")
            .list())
            .thenReturn(listOf(variableInstance))

        val resolver = processVariableValueResolver.createResolver(
            documentId = documentInstanceId
        )

        Assertions.assertThat(resolver.apply("/person/firstName")).isEqualTo("John")
        Assertions.assertThat(resolver.apply("/person/birthDate")).isEqualTo("2000-01-01")
        Assertions.assertThat(resolver.apply("/person/verified")).isEqualTo(true)
        Assertions.assertThat(resolver.apply("/person/location/streetName")).isEqualTo("Funenpark")
        Assertions.assertThat(resolver.apply("/person/location/streetNumber")).isEqualTo(1)
        Assertions.assertThat(resolver.apply("/person/location")).isEqualTo(mapOf("streetName" to "Funenpark", "streetNumber" to 1))
        Assertions.assertThat(resolver.apply("/person")).isEqualTo(objectMapper.convertValue<Map<String, Any?>>(personVariable))
        Assertions.assertThat(resolver.apply("person.firstName")).isEqualTo("John")
        Assertions.assertThat(resolver.apply("person.birthDate")).isEqualTo("2000-01-01")
        Assertions.assertThat(resolver.apply("person.verified")).isEqualTo(true)
        Assertions.assertThat(resolver.apply("person.location.streetName")).isEqualTo("Funenpark")
        Assertions.assertThat(resolver.apply("person.location.streetNumber")).isEqualTo(1)
        Assertions.assertThat(resolver.apply("person.location")).isEqualTo(mapOf("streetName" to "Funenpark", "streetNumber" to 1))
        Assertions.assertThat(resolver.apply("person")).isEqualTo(objectMapper.convertValue<Map<String, Any?>>(personVariable))
    }

    @Test
    fun `should handle value from process variables`() {
        val variableScope = mock<DelegateTaskFake>()
        val processInstanceId = UUID.randomUUID().toString()

        processVariableValueResolver.handleValues(
            processInstanceId, variableScope, mapOf("firstName" to "John")
        )

        verify(runtimeService).setVariables(processInstanceId, mapOf("firstName" to "John"))
    }

    @Test
    fun `should handle value from process variables from runtimeService`() {
        val processInstanceId = UUID.randomUUID().toString()

        processVariableValueResolver.handleValues(
            processInstanceId, null, mapOf("firstName" to "John")
        )

        verify(runtimeService).setVariables(processInstanceId, mapOf("firstName" to "John"))
    }

    @Test
    fun `should handle json values from process variables`() {
        val processInstanceId = UUID.randomUUID().toString()

        processVariableValueResolver.handleValues(
            processInstanceId, null, mapOf("person.firstName" to "John")
        )

        verify(runtimeService).setVariables(processInstanceId, mapOf("person" to mapOf("firstName" to "John")))
    }

    @Test
    fun `should handle json values and merge them with existing process variables`() {
        val processInstanceId = UUID.randomUUID().toString()
        whenever(runtimeService.getVariables(processInstanceId, listOf("person")))
            .thenReturn(mapOf("person" to mapOf("firstName" to "John", "lastName" to "Doe")))

        processVariableValueResolver.handleValues(
            processInstanceId, null, mapOf("person.firstName" to "Hans")
        )

        verify(runtimeService).setVariables(
            processInstanceId,
            mapOf("person" to mapOf("firstName" to "Hans", "lastName" to "Doe"))
        )
    }

    @Test
    fun `should handle json arrays and merge them with existing process variables`() {
        val processInstanceId = UUID.randomUUID().toString()
        whenever(runtimeService.getVariables(processInstanceId, listOf("employees")))
            .thenReturn(mapOf("employees" to listOf(mapOf("firstName" to "John", "lastName" to "Doe"))))

        processVariableValueResolver.handleValues(
            processInstanceId, null, mapOf("employees.0.firstName" to "Hans", "employees.-.firstName" to "Peter")
        )

        verify(runtimeService).setVariables(
            processInstanceId,
            mapOf(
                "employees" to listOf(
                    mapOf("firstName" to "Hans", "lastName" to "Doe"),
                    mapOf("firstName" to "Peter")
                )
            )
        )
    }

    @Test
    fun `should add property to object in a single handleValues`() {
        val processInstanceId = UUID.randomUUID().toString()

        processVariableValueResolver.handleValues(
            processInstanceId, null, mapOf(
                "person.info" to mapOf("firstName" to "John"),
                "person.info.lastName" to "Doe"
            )
        )

        verify(runtimeService).setVariables(
            processInstanceId,
            mapOf("person" to mapOf("info" to mapOf("firstName" to "John", "lastName" to "Doe")))
        )
    }
}
