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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginActionDefinitionId
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.exception.PluginPropertyParseException
import com.ritense.plugin.exception.PluginPropertyRequiredException
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginConfigurationSearchRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import javax.validation.Validation
import kotlin.test.assertEquals

internal class PluginServiceTest {

    lateinit var pluginDefinitionRepository: PluginDefinitionRepository
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository
    lateinit var pluginActionDefinitionRepository: PluginActionDefinitionRepository
    lateinit var pluginProcessLinkRepository: PluginProcessLinkRepository
    lateinit var pluginFactory: PluginFactory<Any>
    lateinit var valueResolverService: ValueResolverService
    lateinit var pluginService: PluginService
    lateinit var pluginConfigurationSearchRepository: PluginConfigurationSearchRepository

    @BeforeEach
    fun init() {
        pluginDefinitionRepository = mock()
        pluginConfigurationRepository = mock()
        pluginActionDefinitionRepository = mock()
        pluginProcessLinkRepository = mock()
        pluginFactory = mock()
        valueResolverService = mock()
        pluginConfigurationSearchRepository = mock()
        pluginService = spy(PluginService(
            pluginDefinitionRepository,
            pluginConfigurationRepository,
            pluginActionDefinitionRepository,
            pluginProcessLinkRepository,
            listOf(pluginFactory),
            jacksonObjectMapper(),
            valueResolverService,
            pluginConfigurationSearchRepository,
            Validation.buildDefaultValidatorFactory().validator
        ))
    }

    @Test
    fun `should get plugin definitions from repository`(){
        pluginService.getPluginDefinitions()
        verify(pluginDefinitionRepository).findAllByOrderByTitleAsc()
    }

    @Test
    fun `should get plugin configurations from repository`(){
        pluginService.getPluginConfigurations(PluginConfigurationSearchParameters())
        verify(pluginConfigurationSearchRepository).search(any())
    }
    @Test
    fun `should save plugin configuration`(){
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val plugin2 = TestPlugin2()
        plugin2.name = "whatever"

        doReturn(plugin2).whenever(pluginService).createInstance(any<PluginConfiguration>())

        pluginService
            .createPluginConfiguration(
                "title", ObjectMapper().readTree("{\"name\": \"whatever\" }") as ObjectNode, "key"
            )
        verify(pluginConfigurationRepository).save(any())
    }

    @Test
    fun `should throw exception when required plugin property field is missing`() {
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val exception = assertThrows(PluginPropertyRequiredException::class.java) {
            pluginService
                .createPluginConfiguration(
                    "title", ObjectMapper().readTree("{}") as ObjectNode, "key"
                )
        }
        assertEquals("Plugin property with name 'name' is required for plugin 'Test Plugin'", exception.message)
    }

    @Test
    fun `should throw exception when required plugin property field is null`() {
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val exception = assertThrows(PluginPropertyRequiredException::class.java) {
            pluginService
                .createPluginConfiguration(
                    "title", ObjectMapper().readTree("{\"name\": null}") as ObjectNode, "key"
                )
        }
        assertEquals("Plugin property with name 'name' is required for plugin 'Test Plugin'", exception.message)
    }

    @Test
    fun `should throw exception when required plugin property field is empty string`() {
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val exception = assertThrows(PluginPropertyRequiredException::class.java) {
            pluginService
                .createPluginConfiguration(
                    "title", ObjectMapper().readTree("{\"name\": \"\"}") as ObjectNode, "key"
                )
        }
        assertEquals("Plugin property with name 'name' is required for plugin 'Test Plugin'", exception.message)
    }

    @Test
    fun `should throw exception when plugin property field has incorrect type`() {
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val exception = assertThrows(PluginPropertyParseException::class.java) {
            pluginService
                .createPluginConfiguration(
                    "title", ObjectMapper().readTree("{\"name\": [\"incorrect-type\"]}") as ObjectNode, "key"
                )
        }
        assertEquals("Plugin property with name 'name' failed to parse for plugin 'Test Plugin'", exception.message)
    }

    @Test
    fun `should update plugin configuration`(){
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)

        val pluginConfigurationCaptor = argumentCaptor<PluginConfiguration>()
        val newProperties = ObjectMapper().readTree("{\"name\": \"whatever\" }")  as ObjectNode

        whenever(pluginConfigurationRepository.getById(pluginConfiguration.id)).thenReturn(pluginConfiguration)

        val plugin2 = TestPlugin2()
        plugin2.name = "whatever"

        doReturn(plugin2).whenever(pluginService).createInstance(any<PluginConfiguration>())

        pluginService
            .updatePluginConfiguration(
                pluginConfiguration.id, "title", newProperties
            )
        verify(pluginConfigurationRepository).save(pluginConfigurationCaptor.capture())

        val capturedPluginConfiguration = pluginConfigurationCaptor.firstValue
        assertEquals("title", capturedPluginConfiguration.title)
        assertEquals(newProperties, capturedPluginConfiguration.properties)
    }

    @Test
    fun `should delete plugin configuration`(){
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)

        val pluginConfigurationId = pluginConfiguration.id

        val plugin2 = TestPlugin2()
        plugin2.name = "whatever"

        whenever(pluginConfigurationRepository.getById(pluginConfiguration.id)).thenReturn(pluginConfiguration)
        doReturn(plugin2).whenever(pluginService).createInstance(any<PluginConfiguration>())

        pluginService.deletePluginConfiguration(pluginConfigurationId)
        verify(pluginConfigurationRepository).deleteById(pluginConfigurationId)
    }

    @Test
    fun `should get plugin action definitions from repository by key`(){
        whenever(pluginActionDefinitionRepository.findByIdPluginDefinitionKey("test")).thenReturn(
            listOf(
                PluginActionDefinition(
                    PluginActionDefinitionId(
                        "some-key",
                        mock()
                    ),
                    "title",
                    "description",
                    "method",
                    listOf(ActivityType.SERVICE_TASK_START)
                )
            )
        )

        val actions = pluginService.getPluginDefinitionActions("test", null)

        verify(pluginActionDefinitionRepository).findByIdPluginDefinitionKey("test")

        assertEquals(1, actions.size)
        assertEquals("some-key", actions[0].key)
        assertEquals("title", actions[0].title)
        assertEquals("description", actions[0].description)
    }

    @Test
    fun `should get plugin action definitions from repository by key and activityType`(){
        whenever(pluginActionDefinitionRepository.findByIdPluginDefinitionKeyAndActivityTypes("test", ActivityType.SERVICE_TASK_START)).thenReturn(
            listOf(
                PluginActionDefinition(
                    PluginActionDefinitionId(
                        "some-key",
                        mock()
                    ),
                    "title",
                    "description",
                    "method",
                    listOf(ActivityType.SERVICE_TASK_START)
                )
            )
        )

        val actions = pluginService.getPluginDefinitionActions("test", ActivityType.SERVICE_TASK_START)

        verify(pluginActionDefinitionRepository).findByIdPluginDefinitionKeyAndActivityTypes("test",
            ActivityType.SERVICE_TASK_START)

        assertEquals(1, actions.size)
        assertEquals("some-key", actions[0].key)
        assertEquals("title", actions[0].title)
        assertEquals("description", actions[0].description)
    }

    @Test
    fun `should invoke delegateExecution method`(){
        val execution = mock<DelegateExecution>()
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            "process",
            "activity",
            Mapper.INSTANCE.get().readTree("{\"test\":123}") as ObjectNode,
            PluginConfigurationId.newId(),
            "test-action",
            ActivityType.SERVICE_TASK_START
        )

        val pluginDefinition = newPluginDefinition()
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)
        val testDependency = mock<TestDependency>()

        whenever(pluginConfigurationRepository.getById(any())).thenReturn(pluginConfiguration)
        whenever(pluginFactory.canCreate(any())).thenReturn(true)
        whenever(pluginFactory.create(any())).thenReturn(TestPlugin(testDependency))
        whenever(execution.processInstanceId).thenReturn("test")
        whenever(valueResolverService.resolveValues(any(), any(), any())).thenReturn(mapOf("test" to 123))

        pluginService.invoke(execution, processLink)

        verify(testDependency).processInt(123)
    }

    @Test
    fun `should throw exception when invoking delegateExecution method with resolved variable where result does not match argument type`(){
        val execution = mock<DelegateExecution>()
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            "process",
            "activity",
            Mapper.INSTANCE.get().readTree("{\"test\":\"test:some-value\"}") as ObjectNode,
            PluginConfigurationId.newId(),
            "test-action",
            ActivityType.SERVICE_TASK_START
        )

        val pluginDefinition = newPluginDefinition()
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)
        val testDependency = mock<TestDependency>()

        whenever(pluginConfigurationRepository.getById(any())).thenReturn(pluginConfiguration)
        whenever(pluginFactory.canCreate(any())).thenReturn(true)
        whenever(pluginFactory.create(any())).thenReturn(TestPlugin(testDependency))
        whenever(execution.processInstanceId).thenReturn("test")
        whenever(valueResolverService.resolveValues(any(), any(), any())).thenReturn(mapOf("test" to 123))

        val exception = assertThrows(InvalidFormatException::class.java) {
            pluginService.invoke(execution, processLink)
        }
    }

    @Test
    fun `should throw exception when invoking delegateExecution method with variable where argument type doesn't match`(){
        val execution = mock<DelegateExecution>()
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            "process",
            "activity",
            Mapper.INSTANCE.get().readTree("{\"test\":\"some-value\"}") as ObjectNode,
            PluginConfigurationId.newId(),
            "test-action",
            ActivityType.SERVICE_TASK_START
        )

        val pluginDefinition = newPluginDefinition()
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)
        val testDependency = mock<TestDependency>()

        whenever(pluginConfigurationRepository.getById(any())).thenReturn(pluginConfiguration)
        whenever(pluginFactory.canCreate(any())).thenReturn(true)
        whenever(pluginFactory.create(any())).thenReturn(TestPlugin(testDependency))
        whenever(execution.processInstanceId).thenReturn("test")

        val exception = assertThrows(InvalidFormatException::class.java) {
            pluginService.invoke(execution, processLink)
        }
    }

    @Test
    fun `should invoke delegateTask method`(){
        val task = mock<DelegateTask>()
        val execution = mock<DelegateExecution>()
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            "process",
            "activity",
            Mapper.INSTANCE.get().readTree("{\"test\":123}") as ObjectNode,
            PluginConfigurationId.newId(),
            "test-action-task",
            ActivityType.SERVICE_TASK_START
        )

        val pluginDefinition = newPluginDefinition()
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)
        val testDependency = mock<TestDependency>()

        whenever(pluginConfigurationRepository.getById(any())).thenReturn(pluginConfiguration)
        whenever(pluginFactory.canCreate(any())).thenReturn(true)
        whenever(pluginFactory.create(any())).thenReturn(TestPlugin(testDependency))
        whenever(task.processInstanceId).thenReturn("test")
        whenever(task.execution).thenReturn(execution)
        whenever(execution.processInstanceId).thenReturn("test")
        whenever(valueResolverService.resolveValues(any(), any(), any())).thenReturn(mapOf("test" to 123))

        pluginService.invoke(task, processLink)

        verify(testDependency).processInt(123)
    }

    @Test
    fun `should throw exception when invoking delegateTask method with resolved variable where result does not match argument type`(){
        val task = mock<DelegateTask>()
        val execution = mock<DelegateExecution>()
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            "process",
            "activity",
            Mapper.INSTANCE.get().readTree("{\"test\":\"test:some-value\"}") as ObjectNode,
            PluginConfigurationId.newId(),
            "test-action-task",
            ActivityType.SERVICE_TASK_START
        )

        val pluginDefinition = newPluginDefinition()
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)
        val testDependency = mock<TestDependency>()

        whenever(pluginConfigurationRepository.getById(any())).thenReturn(pluginConfiguration)
        whenever(pluginFactory.canCreate(any())).thenReturn(true)
        whenever(pluginFactory.create(any())).thenReturn(TestPlugin(testDependency))
        whenever(task.processInstanceId).thenReturn("test")
        whenever(task.execution).thenReturn(execution)
        whenever(execution.processInstanceId).thenReturn("test")
        whenever(valueResolverService.resolveValues(any(), any(), any())).thenReturn(mapOf("test" to 123))

        val exception = assertThrows(InvalidFormatException::class.java) {
            pluginService.invoke(task, processLink)
        }
    }

    @Test
    fun `should throw exception when invoking delegateTask method with variable where argument type doesn't match`(){
        val task = mock<DelegateTask>()
        val execution = mock<DelegateExecution>()
        val processLink = PluginProcessLink(
            PluginProcessLinkId.newId(),
            "process",
            "activity",
            Mapper.INSTANCE.get().readTree("{\"test\":\"some-value\"}") as ObjectNode,
            PluginConfigurationId.newId(),
            "test-action-task",
            ActivityType.SERVICE_TASK_START
        )

        val pluginDefinition = newPluginDefinition()
        val pluginConfiguration = newPluginConfiguration(pluginDefinition)
        val testDependency = mock<TestDependency>()

        whenever(pluginConfigurationRepository.getById(any())).thenReturn(pluginConfiguration)
        whenever(pluginFactory.canCreate(any())).thenReturn(true)
        whenever(pluginFactory.create(any())).thenReturn(TestPlugin(testDependency))
        whenever(task.processInstanceId).thenReturn("test")
        whenever(task.execution).thenReturn(execution)
        whenever(execution.processInstanceId).thenReturn("test")

        val exception = assertThrows(InvalidFormatException::class.java) {
            pluginService.invoke(task, processLink)
        }
    }

    private fun newPluginDefinition(): PluginDefinition {
        val pluginDefinition = PluginDefinition(
            "TestPlugin",
            "Test Plugin",
            "description",
            TestPlugin::class.java.name,
            mutableSetOf()
        )
        whenever(pluginDefinitionRepository.getById("key")).thenReturn(pluginDefinition)
        return pluginDefinition
    }

    private fun addPluginProperty(pluginDefinition: PluginDefinition) {
        (pluginDefinition.properties as MutableSet).add(
            PluginProperty(
                "property1",
                pluginDefinition,
                "property1",
                true,
                false,
                "name",
                String::class.java.name
            )
        )
    }

    private fun newPluginConfiguration(pluginDefinition: PluginDefinition): PluginConfiguration {
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().readTree("{\"name\": \"whatever\" }") as ObjectNode,
            pluginDefinition
        )
        whenever(pluginConfigurationRepository.save(any())).thenReturn(pluginConfiguration)
        return pluginConfiguration
    }

    class TestPlugin(
        val testDependency: TestDependency
    ) {
        @PluginAction(
            key = "test-action",
            title = "Test action",
            description = "This is an action used to verify plugin framework functionality",
            activityTypes = [ActivityType.SERVICE_TASK_START]
        )
        fun doThing(@PluginActionProperty test: Int) {
            testDependency.processInt(test)
        }

        @PluginAction(
            key = "test-action-task",
            title = "Test action task",
            description = "This is an action used to verify plugin framework functionality",
            activityTypes = [ActivityType.USER_TASK_CREATE]
        )
        fun doThing2(@PluginActionProperty test: Int) {
            testDependency.processInt(test)
        }
    }

    class TestPlugin2 {
        @com.ritense.plugin.annotation.PluginProperty(key = "name", required = false, secret = false)
        var name: String? = null
    }

    interface TestDependency{
        fun processInt(test: Int)
    }
}
