package com.ritense.valtimo.formflow.handler

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
import com.ritense.valtimo.formflow.handler.FormFlowCreateTaskEventHandler
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd
import org.camunda.bpm.engine.impl.cmd.CreateTaskCmd
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.Task
import org.camunda.community.mockito.QueryMocks
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.util.Optional

class FormFlowCreateTaskEventhandlerTest {
    lateinit var formFlowService: FormFlowService
    lateinit var formAssociationService: FormAssociationService
    lateinit var formFlowCreateTaskEventHandler: FormFlowCreateTaskEventHandler

    @BeforeEach
    fun init() {
        formFlowService = mock()
        formAssociationService = mock()
        formFlowCreateTaskEventHandler = FormFlowCreateTaskEventHandler(formFlowService, formAssociationService)
    }

    @Test
    fun `intercepts CreateTaskCmd and handle correctly`() {
        val task: DelegateTask = mock()
        val execution: ExecutionEntity = mock()
        val processDefinition: ProcessDefinitionEntity = mock()
        val formAssociation: FormAssociation = mock()
        val formLink: BpmnElementFormFlowIdLink = mock()
        val formFlowDefinition: FormFlowDefinition = mock()
        val formFlowInstance: FormFlowInstance = mock()
        whenever(task.execution).thenReturn(execution)
        whenever(execution.processDefinition).thenReturn(processDefinition)
        whenever(formAssociation.formLink).thenReturn(formLink)
        whenever(formLink.formFlowId).thenReturn("abc:1")

        whenever(formAssociationService
            .getFormAssociationByFormLinkId(null, null)).thenReturn(Optional.of(formAssociation))

        whenever(formFlowService.findDefinition(any())).thenReturn(formFlowDefinition)
        whenever(formFlowDefinition.createInstance(any())).thenReturn(formFlowInstance)

        formFlowCreateTaskEventHandler.handle(task)

        verify(formFlowService).save(formFlowInstance)
    }

    @Test
    fun `intercepts CreateTaskCmd without formLink and does not save FormFlowInstance`() {
        val task: DelegateTask = mock()
        val execution: ExecutionEntity = mock()
        val processDefinition: ProcessDefinitionEntity = mock()
        val formAssociation: FormAssociation = mock()
        val formLink: BpmnElementFormFlowIdLink = mock()
        val formFlowDefinition: FormFlowDefinition = mock()
        val formFlowInstance: FormFlowInstance = mock()
        whenever(task.execution).thenReturn(execution)
        whenever(execution.processDefinition).thenReturn(processDefinition)
        whenever(formAssociation.formLink).thenReturn(formLink)
        whenever(formLink.formFlowId).thenReturn("abc:1")

        whenever(formAssociationService
            .getFormAssociationByFormLinkId(null, null)).thenReturn(Optional.empty())

        whenever(formFlowService.findDefinition(any())).thenReturn(formFlowDefinition)
        whenever(formFlowDefinition.createInstance(any())).thenReturn(formFlowInstance)

        verify(formFlowService, times(0)).save(formFlowInstance)
    }
}