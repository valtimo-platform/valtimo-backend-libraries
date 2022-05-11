package com.ritense.valtimo.formflow.handler

import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.service.FormAssociationService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.springframework.context.event.EventListener

class FormFlowCreateTaskEventHandler(
    val formFlowService: FormFlowService,
    val formAssociationService: FormAssociationService
) {

    @EventListener(condition = "#task.eventName=='create'")
    fun handle(task: DelegateTask) {
        val formAssociationOptional = formAssociationService
            .getFormAssociationByFormLinkId(
                (task.execution as ExecutionEntity).processDefinition.key,
                task.taskDefinitionKey
            )

        if (formAssociationOptional.isPresent &&
            formAssociationOptional.get().formLink is BpmnElementFormFlowIdLink) {

            createFormFlowInstance(task, formAssociationOptional.get())
        }
    }

    private fun createFormFlowInstance(task: DelegateTask,
        formAssociation: FormAssociation
    ) {
        val additionalProperties: MutableMap<String, Any> = getAdditionalProperties(task)
        val formFlowIdAsArray = formAssociation.formLink.formFlowId.split(":")
        val formFlowDefinition: FormFlowDefinition = getFormFlowDefinition(formFlowIdAsArray)
        formFlowService.save(formFlowDefinition.createInstance(additionalProperties))
    }

    private fun getFormFlowDefinition(formFlowIdAsArray: List<String>): FormFlowDefinition {
        val formFlowDefinition: FormFlowDefinition

        if (formFlowIdAsArray[1] == "latest") {
            formFlowDefinition = formFlowService.findLatestDefinitionByKey(formFlowIdAsArray[0])!!
        } else {
            formFlowDefinition = formFlowService.findDefinition(
                FormFlowDefinitionId(formFlowIdAsArray[0], formFlowIdAsArray[1].toLong())
            )
        }

        return formFlowDefinition
    }

    private fun getAdditionalProperties(task: DelegateTask): MutableMap<String, Any> {

        return mutableMapOf("processInstanceId" to task.processInstanceId,
            "processInstanceBusinessKey" to task.execution.processBusinessKey,
            "taskId" to task.id
        )

    }
}