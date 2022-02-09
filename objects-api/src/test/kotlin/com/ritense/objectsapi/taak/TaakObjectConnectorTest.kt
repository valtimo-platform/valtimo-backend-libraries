package com.ritense.objectsapi.taak

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.taak.initiator.BsnProvider
import com.ritense.objectsapi.taak.initiator.KvkProvider
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolverService
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

internal class TaakObjectConnectorTest {

    private lateinit var taakProperties: TaakProperties
    private lateinit var placeHolderValueResolverService: PlaceHolderValueResolverService
    private lateinit var bsnProvider: BsnProvider
    private lateinit var kvkProvider: KvkProvider

    private lateinit var taakObjectConnector: TaakObjectConnector

    @BeforeEach
    internal fun setUp() {
        taakProperties = TaakProperties()
        placeHolderValueResolverService = mock()
        bsnProvider = mock()
        kvkProvider = mock()
        taakObjectConnector = spy(
            TaakObjectConnector(
                taakProperties,
                placeHolderValueResolverService,
                bsnProvider,
                kvkProvider
            )
        )
    }

    @Test
    fun `should create object with properties from task`() {
        whenever(bsnProvider.getBurgerServiceNummer(any())).thenReturn("my-bsn")
        doReturn(mock<com.ritense.objectsapi.domain.Object>()).whenever(taakObjectConnector).createObject(any())
        val task = mockDelegateTask("taak:my-var", "pv:my-process-var")

        taakObjectConnector.createTask(task, "my-form-id")

        val captor = argumentCaptor<CreateObjectRequest>()
        verify(taakObjectConnector).createObject(captor.capture())
        Assertions.assertThat(captor.firstValue.record.data).containsAllEntriesOf(
            mapOf(
                "bsn" to "my-bsn",
                "data" to mapOf("my-var" to "pv:my-process-var"),
                "formulier_id" to "my-form-id"
            )
        )
    }

    @Test
    fun `should not use task-properties that are not marked with 'taak'`() {
        whenever(bsnProvider.getBurgerServiceNummer(any())).thenReturn("my-bsn")
        doReturn(mock<com.ritense.objectsapi.domain.Object>()).whenever(taakObjectConnector).createObject(any())
        val task = mockDelegateTask("my-var", "doc:/my-doc-prop")

        taakObjectConnector.createTask(task, "my-form-id")

        val captor = argumentCaptor<CreateObjectRequest>()
        verify(taakObjectConnector).createObject(captor.capture())
        Assertions.assertThat(captor.firstValue.record.data).containsAllEntriesOf(
            mapOf(
                "data" to emptyMap<String, String>(),
            )
        )
    }

    private fun mockDelegateTask(propertyName: String, propertyValue: String): DelegateTask {
        val camundaProperty = mock<CamundaProperty>()
        whenever(camundaProperty.camundaName).thenReturn(propertyName)
        whenever(camundaProperty.camundaValue).thenReturn(propertyValue)

        val camundaProperties = mock<CamundaProperties>()
        whenever(camundaProperties.camundaProperties).thenReturn(listOf(camundaProperty))

        val delegateTask = Mockito.mock(DelegateTask::class.java, Mockito.RETURNS_DEEP_STUBS)
        whenever(delegateTask.executionId).thenReturn(UUID.randomUUID().toString())
        whenever(delegateTask.bpmnModelElementInstance.extensionElements.elements).thenReturn(listOf(camundaProperties))
        whenever(delegateTask.processInstanceId).thenReturn(UUID.randomUUID().toString())

        return delegateTask
    }

}
