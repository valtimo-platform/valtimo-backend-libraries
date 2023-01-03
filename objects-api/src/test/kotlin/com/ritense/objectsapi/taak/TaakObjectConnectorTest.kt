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

package com.ritense.objectsapi.taak

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.service.ObjectsApiConnector
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.openzaak.provider.BsnProvider
import com.ritense.openzaak.provider.KvkProvider
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.valueresolver.ValueResolverService
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
internal class TaakObjectConnectorTest {

    private lateinit var taakProperties: TaakProperties
    private lateinit var valueResolverService: ValueResolverService
    private lateinit var connectorService: ConnectorService
    private lateinit var bsnProvider: BsnProvider
    private lateinit var kvkProvider: KvkProvider

    private lateinit var taakObjectConnector: TaakObjectConnector

    @BeforeEach
    internal fun setUp() {
        taakProperties = TaakProperties()
        valueResolverService = mock()
        connectorService = mock()
        bsnProvider = mock()
        kvkProvider = mock()
        taakObjectConnector = spy(
            TaakObjectConnector(
                taakProperties,
                valueResolverService,
                connectorService,
                bsnProvider,
                kvkProvider
            )
        )
    }

    @Test
    fun `should create object with properties from task`() {
        val objectsApiConnector = mock<ObjectsApiConnector>()
        whenever(bsnProvider.getBurgerServiceNummer(any())).thenReturn("my-bsn")
        whenever(connectorService.loadByName(any())).thenReturn(objectsApiConnector)
        whenever(objectsApiConnector.getProperties()).thenReturn(ObjectsApiProperties())
        doReturn(mock<com.ritense.objectsapi.domain.Object>()).whenever(objectsApiConnector).createObject(any())
        val task = mockDelegateTask("my-task-name", "taak:my-var", "pv:my-process-var")

        whenever(
            valueResolverService.resolveValues(
                eq(task.processInstanceId),
                eq(task),
                eq(listOf("pv:my-process-var"))
            )
        ).thenReturn(mapOf("pv:my-process-var" to "somevalue"))

        taakObjectConnector.createTask(task, "my-form-id")

        val captor = argumentCaptor<CreateObjectRequest>()
        verify(objectsApiConnector).createObject(captor.capture())
        val capturedObjectRequest = captor.firstValue
        Assertions.assertThat(capturedObjectRequest.record.data).containsAllEntriesOf(
            mapOf(
                "bsn" to "my-bsn",
                "data" to mapOf("my-var" to "somevalue"),
                "formulier_id" to "my-form-id",
                "title" to "my-task-name"
            )
        )
    }

    @Test
    fun `should create task object with formulier_url property`() {
        val objectsApiConnector = mock<ObjectsApiConnector>()
        whenever(bsnProvider.getBurgerServiceNummer(any())).thenReturn("my-bsn")
        whenever(connectorService.loadByName(any())).thenReturn(objectsApiConnector)
        whenever(objectsApiConnector.getProperties()).thenReturn(ObjectsApiProperties())
        doReturn(mock<com.ritense.objectsapi.domain.Object>()).whenever(objectsApiConnector).createObject(any())
        val task = mockDelegateTask("my-task-name", "taak:my-var", "pv:my-process-var")

        whenever(
            valueResolverService.resolveValues(
                eq(task.processInstanceId),
                eq(task),
                eq(listOf("pv:my-process-var"))
            )
        ).thenReturn(mapOf("pv:my-process-var" to "somevalue"))

        taakObjectConnector.createTaskWithFormUrl(task, "http://localhost:8000/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777")

        val captor = argumentCaptor<CreateObjectRequest>()
        verify(objectsApiConnector).createObject(captor.capture())
        val capturedObjectRequest = captor.firstValue
        Assertions.assertThat(capturedObjectRequest.record.data).containsAllEntriesOf(
            mapOf(
                "bsn" to "my-bsn",
                "data" to mapOf("my-var" to "somevalue"),
                "formulier_url" to "http://localhost:8000/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777",
                "title" to "my-task-name"
            )
        )
    }

    @Test
    fun `should not use task-properties that are not marked with 'taak'`() {
        val objectsApiConnector = mock<ObjectsApiConnector>()
        whenever(bsnProvider.getBurgerServiceNummer(any())).thenReturn("my-bsn")
        whenever(connectorService.loadByName(any())).thenReturn(objectsApiConnector)
        whenever(objectsApiConnector.getProperties()).thenReturn(ObjectsApiProperties())
        doReturn(mock<com.ritense.objectsapi.domain.Object>()).whenever(objectsApiConnector).createObject(any())
        val task = mockDelegateTask("my-task-name", "my-var", "doc:/my-doc-prop")

        taakObjectConnector.createTask(task, "my-form-id")

        val captor = argumentCaptor<CreateObjectRequest>()
        verify(objectsApiConnector).createObject(captor.capture())
        Assertions.assertThat(captor.firstValue.record.data).containsAllEntriesOf(
            mapOf(
                "data" to emptyMap<String, String>(),
            )
        )
    }

    private fun mockDelegateTask(taskName: String, propertyName: String, propertyValue: String): DelegateTask {
        val camundaProperty = mock<CamundaProperty>()
        whenever(camundaProperty.camundaName).thenReturn(propertyName)
        whenever(camundaProperty.camundaValue).thenReturn(propertyValue)

        val camundaProperties = mock<CamundaProperties>()
        whenever(camundaProperties.camundaProperties).thenReturn(listOf(camundaProperty))

        val delegateTask = Mockito.mock(DelegateTask::class.java, Mockito.RETURNS_DEEP_STUBS)
        whenever(delegateTask.id).thenReturn(UUID.randomUUID().toString())
        whenever(delegateTask.name).thenReturn(taskName)
        whenever(delegateTask.bpmnModelElementInstance.extensionElements.elements).thenReturn(listOf(camundaProperties))
        whenever(delegateTask.processInstanceId).thenReturn(UUID.randomUUID().toString())

        return delegateTask
    }

}
