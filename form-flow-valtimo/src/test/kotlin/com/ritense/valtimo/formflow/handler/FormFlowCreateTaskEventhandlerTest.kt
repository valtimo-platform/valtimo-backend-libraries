package com.ritense.valtimo.formflow.handler

import com.ritense.document.service.DocumentService
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.service.FormAssociationService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class FormFlowCreateTaskEventhandlerTest {
    lateinit var formFlowService: FormFlowService
    lateinit var formAssociationService: FormAssociationService
    lateinit var formFlowCreateTaskEventHandler: FormFlowCreateTaskEventHandler
    lateinit var documentService: DocumentService

    @BeforeEach
    fun init() {
        formFlowService = mock()
        formAssociationService = mock()
        documentService = mock()
        formFlowCreateTaskEventHandler = FormFlowCreateTaskEventHandler(
            formFlowService, formAssociationService, documentService)
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