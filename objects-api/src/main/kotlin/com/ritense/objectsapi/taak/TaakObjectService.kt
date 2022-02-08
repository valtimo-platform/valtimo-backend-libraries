package com.ritense.objectsapi.taak

import com.ritense.objectsapi.taak.initiator.BsnProvider
import com.ritense.objectsapi.taak.initiator.KvkProvider
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolverService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import java.util.UUID
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties

//TODO: make this a connector
class TaakObjectService(
    private val placeHolderValueResolverService: PlaceHolderValueResolverService,
    private val bsnProvider: BsnProvider,
    private val kvkProvider: KvkProvider
) {

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

    private fun resolveValue(task: DelegateTask, value: String): Any {
        return placeHolderValueResolverService.resolveValue(
            placeholder = value,
            processInstanceId = CamundaProcessInstanceId(task.processInstanceId),
            variableScope = task
        ) ?: value
    }
}