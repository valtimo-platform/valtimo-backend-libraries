package com.ritense.valtimo.formflow.interceptor

import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.service.FormAssociationService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.cmd.CreateTaskCmd
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor
import org.camunda.bpm.engine.task.Task
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.lang.reflect.Field

@Component
class FormFlowCreateTaskCommandInterceptor: CommandInterceptor() {
    lateinit var taskService: TaskService
    lateinit var runtimeService: RuntimeService
    lateinit var formFlowService: FormFlowService
    lateinit var formAssociationService: FormAssociationService

    override fun <T : Any?> execute(command: Command<T>?): T {
        if (command is CreateTaskCmd) {
            val task = getTask(command as CreateTaskCmd)

            val formAssociationOptional = formAssociationService
                .getFormAssociationByFormLinkId(task.processDefinitionId, task.taskDefinitionKey)

            if (formAssociationOptional.isPresent &&
                formAssociationOptional.get().formLink is BpmnElementFormFlowIdLink) {

                createFormFlowInstance(task, formAssociationOptional.get())
            }
        }

        return proceed(command)
    }

    @Autowired
    fun setDependencies(@Lazy taskService: TaskService,
        @Lazy runtimeService: RuntimeService,
        formFlowService: FormFlowService,
        @Lazy formAssociationService: FormAssociationService
    ) {
        this.taskService = taskService
        this.runtimeService = runtimeService
        this.formFlowService = formFlowService
        this.formAssociationService = formAssociationService
    }

    private fun getTask(command: CreateTaskCmd): Task {
        val field: Field = CreateTaskCmd::class.java.getDeclaredField("taskId")
        field.isAccessible = true
        val taskId: String = field.get(command) as String
        return taskService.createTaskQuery().taskId(taskId).singleResult()
    }

    private fun createFormFlowInstance(task: Task, formAssociation: FormAssociation) {
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

    private fun getAdditionalProperties(task: Task): MutableMap<String, Any> {
        val processInstance = runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(task.processInstanceId)
            .singleResult()

        return mutableMapOf("processInstanceId" to processInstance.id,
            "processInstanceBusinessKey" to processInstance.businessKey,
            "taskId" to task.id
        )

    }

    private fun <T : Any?> proceed (command: Command<T>?): T {
        return next.execute(command)
    }

}