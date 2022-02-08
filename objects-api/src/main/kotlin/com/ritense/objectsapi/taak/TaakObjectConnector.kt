package com.ritense.objectsapi.taak

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.objectsapi.domain.Record
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.service.ObjectsApiService
import com.ritense.objectsapi.taak.initiator.BsnProvider
import com.ritense.objectsapi.taak.initiator.KvkProvider
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolverService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.valtimo.contract.json.Mapper
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties

@ConnectorType(name = "Taak")
class TaakObjectConnector(
    private var taakProperties: TaakProperties,
    private val placeHolderValueResolverService: PlaceHolderValueResolverService,
    private val bsnProvider: BsnProvider,
    private val kvkProvider: KvkProvider
):Connector, ObjectsApiService(taakProperties.objectsApiProperties) {

    fun createTask(task: DelegateTask, formulierId: String) {
        val taakObject = createTaakObjectDto(task, formulierId)

        createObjectRecord(taakObject)
    }

    private fun createObjectRecord(taakObject: TaakObjectDto) {
        val objectType = objectsApiProperties.objectType
        createObject(
            CreateObjectRequest(
                type = URI(objectType.url),
                Record(
                    typeVersion = objectType.typeVersion,
                    startAt = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()),
                    data = Mapper.INSTANCE.get().convertValue(taakObject, jacksonTypeRef<Map<String, Any>>())
                )
            )
        )
    }

    private fun createTaakObjectDto(
        task: DelegateTask,
        formulierId: String
    ): TaakObjectDto {
        val taakObject = TaakObjectDto(
            bsn = bsnProvider.getBurgerServiceNummer(task),
            kvk = kvkProvider.getKvkNummer(task),
            verwerkerTaakId = UUID.fromString(task.executionId),
            formulierId = formulierId,
            data = getTaskProperties(task),
            status = TaakObjectStatus.open
        )
        return taakObject
    }

    private fun getTaskProperties(task: DelegateTask): Map<String, Any> {
        task.bpmnModelInstance
        return task.bpmnModelElementInstance.extensionElements.elements
            .filterIsInstance<CamundaProperties>()
            .single()
            .camundaProperties
            .filter { it.camundaName.startsWith(prefix = "taak:", ignoreCase = true) }
            .associateBy(
                { it.camundaName.substringAfter(delimiter = ":") },
                { resolveValue(task, it.camundaValue) }
            )
    }

    private fun resolveValue(task: DelegateTask, value: String): Any {
        return placeHolderValueResolverService.resolveValue(
            placeholder = value,
            processInstanceId = CamundaProcessInstanceId(task.processInstanceId),
            variableScope = task
        ) ?: value
    }

    override fun getProperties(): ConnectorProperties {
        return taakProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        taakProperties = connectorProperties as TaakProperties
        objectsApiProperties = taakProperties.objectsApiProperties
    }
}