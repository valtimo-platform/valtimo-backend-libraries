package com.ritense.objectsapi.taak

import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.objectsapi.service.ObjectsApiService
import java.util.UUID
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service

@Service
@ConditionalOnBean(name = [TaakObjectService.TAAK_OBJECT_API_PROPERTIES_BEAN])
class TaakObjectService(
    @Qualifier(value = TAAK_OBJECT_API_PROPERTIES_BEAN) objectsApiProperties:ObjectsApiProperties,
    private val bsnProvider:BsnProvider,
    private val kvkProvider:KvkProvider
) {
    private val objectsApiService = ObjectsApiService(objectsApiProperties)

    fun createTask(formulierId: String, task: DelegateTask) {
        TaakObjectDto(
            bsn = bsnProvider.getBurgerServiceNummer(task),
            kvk = kvkProvider.getKvkNummer(task),
            verwerkerTaakId = UUID.fromString(task.executionId),
            formulierId = formulierId,
            data = getTaskProperties(task),
            status = TaakObjectStatus.open
        )
        //TODO: save in objects API
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

    private fun resolveValue(task: DelegateTask, value: String): String {
        //TODO: add functionality to translate doc:,pv: and other references.
        return value
    }

    companion object {
        const val TAAK_OBJECT_API_PROPERTIES_BEAN = "taakObjectsApiProperties"
    }
}