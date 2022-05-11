package com.ritense.valtimo.formflow.interceptor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.service.FormAssociationService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd
import org.camunda.bpm.engine.impl.cmd.CreateTaskCmd
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.Task
import org.camunda.community.mockito.QueryMocks
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.util.Optional

class FormFlowCreateTaskCommandInterceptorTest {
    lateinit var taskService: TaskService
    lateinit var runtimeService: RuntimeService
    lateinit var formFlowService: FormFlowService
    lateinit var formAssociationService: FormAssociationService
    lateinit var formFlowCreateTaskCommandInterceptor: FormFlowCreateTaskCommandInterceptor

    @BeforeEach
    fun init() {
        taskService = mock()
        runtimeService = mock()
        formFlowService = mock()
        formAssociationService = mock()
        formFlowCreateTaskCommandInterceptor = FormFlowCreateTaskCommandInterceptor()
        formFlowCreateTaskCommandInterceptor
            .setDependencies(taskService, runtimeService, formFlowService, formAssociationService)
        val commandExecutor: CommandExecutor = mock()
        formFlowCreateTaskCommandInterceptor.next = commandExecutor
    }

    @Test
    fun `intercepts CreateTaskCmd and handle correctly`() {
        val command: CreateTaskCmd = mock()
        val field: Field = CreateTaskCmd::class.java.getDeclaredField("taskId")
        field.isAccessible = true
        field.set(command, "test123")

        val task: Task = mock()
        val processInstance: ProcessInstance = mock()
        val formAssociation: FormAssociation = mock()
        val formLink: BpmnElementFormFlowIdLink = mock()
        val formFlowDefinition: FormFlowDefinition = mock()
        val formFlowInstance: FormFlowInstance = mock()
        whenever(formAssociation.formLink).thenReturn(formLink)
        whenever(formLink.formFlowId).thenReturn("abc:1")

        QueryMocks.mockTaskQuery(taskService).singleResult(task)
        QueryMocks.mockProcessInstanceQuery(runtimeService).singleResult(processInstance)

        whenever(formAssociationService
            .getFormAssociationByFormLinkId(null, null)).thenReturn(Optional.of(formAssociation))

        whenever(formFlowService.findDefinition(any())).thenReturn(formFlowDefinition)
        whenever(formFlowDefinition.createInstance(any())).thenReturn(formFlowInstance)

        formFlowCreateTaskCommandInterceptor.execute(command)

        verify(formFlowService).save(formFlowInstance)
    }

    @Test
    fun `intercepts CreateTaskCmd without formLink and does not save FormFlowInstance`() {
        val command: CreateTaskCmd = mock()
        val field: Field = CreateTaskCmd::class.java.getDeclaredField("taskId")
        field.isAccessible = true
        field.set(command, "test123")

        val task: Task = mock()
        val processInstance: ProcessInstance = mock()
        val formAssociation: FormAssociation = mock()
        val formLink: BpmnElementFormFlowIdLink = mock()
        val formFlowDefinition: FormFlowDefinition = mock()
        val formFlowInstance: FormFlowInstance = mock()
        whenever(formAssociation.formLink).thenReturn(formLink)
        whenever(formLink.formFlowId).thenReturn("abc:1")

        QueryMocks.mockTaskQuery(taskService).singleResult(task)
        QueryMocks.mockProcessInstanceQuery(runtimeService).singleResult(processInstance)

        whenever(formAssociationService
            .getFormAssociationByFormLinkId(null, null)).thenReturn(Optional.empty())

        whenever(formFlowService.findDefinition(any())).thenReturn(formFlowDefinition)
        whenever(formFlowDefinition.createInstance(any())).thenReturn(formFlowInstance)

        formFlowCreateTaskCommandInterceptor.execute(command)

        verify(formFlowService, times(0)).save(formFlowInstance)
    }

    @Test
    fun `intercepts invalid command and does not save FormFlowInstance`() {
        val command: CompleteTaskCmd = mock()

        formFlowCreateTaskCommandInterceptor.execute(command)

        verify(formFlowService, times(0)).save(any<FormFlowInstance>())
    }
}