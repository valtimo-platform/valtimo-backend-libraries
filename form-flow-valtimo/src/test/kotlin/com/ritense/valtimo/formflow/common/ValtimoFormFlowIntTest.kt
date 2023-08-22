/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.formflow.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.service.FormFlowDeploymentService
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType
import com.ritense.formlink.domain.request.CreateFormAssociationRequest
import com.ritense.formlink.domain.request.FormLinkRequest
import com.ritense.formlink.service.FormAssociationService
import com.ritense.formlink.service.ProcessLinkService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.formflow.BaseIntegrationTest
import com.ritense.valtimo.formflow.FormFlowTaskOpenResultProperties
import com.ritense.valtimo.formflow.web.rest.FormFlowResource
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class ValtimoFormFlowIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var formAssociationService: FormAssociationService

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var processLinkService: ProcessLinkService

    @Autowired
    lateinit var docummentService: DocumentService

    @Autowired
    lateinit var newProcessLinkService: com.ritense.processlink.service.ProcessLinkService

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Autowired
    lateinit var formFlowResource: FormFlowResource

    @Autowired
    lateinit var formFlowDeploymentService: FormFlowDeploymentService

    @Autowired
    lateinit var historyService: HistoryService

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var processLinkActivityHandler: ProcessLinkActivityHandler<FormFlowTaskOpenResultProperties>

    @Test
    fun `should completeTask`() {
        deployFormFlow(onComplete = "\${valtimoFormFlow.completeTask(additionalProperties)}")
        linkFormFlowToUserTask()
        val documentAndProcess = newDocumentAndStartProcess()
        val processInstanceId = documentAndProcess.resultingProcessInstanceId().get()
        val formFlowInstance = openTasks(processInstanceId).single()

        formFlowStepComplete(formFlowInstance)

        assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstanceId.toString()).count())
    }

    @Test
    fun `should completeTask and save submission in document`() {
        deployFormFlow(onComplete = "\${valtimoFormFlow.completeTask(additionalProperties, step.submissionData)}")
        linkFormFlowToUserTask()
        val documentAndProcess = newDocumentAndStartProcess()
        val processInstanceId = documentAndProcess.resultingProcessInstanceId().get()
        val formFlowInstance = openTasks(processInstanceId).single()

        formFlowStepComplete(formFlowInstance, submission = """{"firstName":"John"}""")

        assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstanceId.toString()).count())
        assertEquals(
            """{"submission":{"firstName":"John"}}""",
            documentAndProcess.resultingDocument().get().content().asJson().toString()
        )
    }

    @Test
    fun `should completeTask and save submission in document and process`() {
        deployFormFlow(onComplete = "\${valtimoFormFlow.completeTask(additionalProperties, step.submissionData, {'doc:/address/streetName':'/street', 'pv:approved':'/approval'})}")
        linkFormFlowToUserTask()
        val documentAndProcess = newDocumentAndStartProcess()
        val processInstanceId = documentAndProcess.resultingProcessInstanceId().get()
        val formFlowInstance = openTasks(processInstanceId).single()

        formFlowStepComplete(formFlowInstance, submission = """{"street":"Funenpark","approval":true}""")

        assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstanceId.toString()).count())
        assertEquals(
            """{"address":{"streetName":"Funenpark"}}""",
            documentAndProcess.resultingDocument().get().content().asJson().toString()
        )
        val processVariableApproved = historyService.createHistoricVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId.toString())
            .variableName("approved")
            .singleResult()
            .value
        assertEquals(true, processVariableApproved)
    }

    @Test
    fun `should start case and save submission in document and process`() {
        deployFormFlow(onComplete = "\${valtimoFormFlow.startCase(instance.id, {'doc:/address/streetName':'/street', 'pv:approved':'/approval'})}")
        val processLink = linkFormFlowToStartEvent()

        val startEventResponse = processLinkActivityHandler.getStartEventObject(
            getProcessDefinitionId(),
            null,
            "profile",
            processLink
        )
        val formFlowInstance = formFlowService
            .getInstanceById(FormFlowInstanceId.existingId(startEventResponse.properties.formFlowInstanceId))

        // number before complete
        val totalDocumentsBefore = docummentService.getAllByDocumentDefinitionName(Pageable.unpaged(), "profile").totalElements

        formFlowStepComplete(formFlowInstance, submission = """{"street":"Koningin Wilhelminaplein","approval":true}""")

        //find latest document
        val allDocuments = docummentService.getAllByDocumentDefinitionName(Pageable.unpaged(), "profile")
        val latestDocument = allDocuments.content.sortedByDescending { it.createdOn() }.first()

        assertEquals(allDocuments.totalElements, totalDocumentsBefore+1)
        assertEquals(
            """{"address":{"streetName":"Koningin Wilhelminaplein"}}""",
            latestDocument.content().asJson().toString()
        )

        val processDocumentInstances =
            processDocumentAssociationService.findProcessDocumentInstances(latestDocument.id())

        assertEquals(1, processDocumentInstances.size)

        val processVariableApproved = historyService.createHistoricVariableInstanceQuery()
            .processInstanceIdIn(processDocumentInstances[0].processDocumentInstanceId().processInstanceId().toString())
            .variableName("approved")
            .singleResult()
            .value
        assertEquals(true, processVariableApproved)
    }

    @Test
    fun `should process for existing case and save submission in document and process`() {
        deployFormFlow(onComplete = "\${valtimoFormFlow.startSupportingProcess(instance.id, {'doc:/address/streetName':'/street', 'pv:approved':'/approval'})}")
        val processLink = linkFormFlowToStartEvent()

        val document = docummentService.createDocument(
            NewDocumentRequest(
                "profile",
                Mapper.INSTANCE.get().readTree("{}")
            )
        ).resultingDocument().get()

        val startEventResponse = processLinkActivityHandler.getStartEventObject(
            getProcessDefinitionId(),
            document.id().id,
            null,
            processLink
        )
        val formFlowInstance = formFlowService
            .getInstanceById(FormFlowInstanceId.existingId(startEventResponse.properties.formFlowInstanceId))

        formFlowStepComplete(formFlowInstance, submission = """{"street":"Koningin Wilhelminaplein","approval":true}""")

        val updatedDocument = docummentService.get(document.id().toString())

        assertEquals(
            """{"address":{"streetName":"Koningin Wilhelminaplein"}}""",
            updatedDocument.content().asJson().toString()
        )

        val processDocumentInstances =
            processDocumentAssociationService.findProcessDocumentInstances(updatedDocument.id())

        assertEquals(1, processDocumentInstances.size)

        val processVariableApproved = historyService.createHistoricVariableInstanceQuery()
            .processInstanceIdIn(processDocumentInstances[0].processDocumentInstanceId().processInstanceId().toString())
            .variableName("approved")
            .singleResult()
            .value
        assertEquals(true, processVariableApproved)
    }

    private fun formFlowStepComplete(formFlowInstance: FormFlowInstance, submission: String? = null) {
        formFlowResource.completeStep(
            formFlowInstance.id.id.toString(),
            formFlowInstance.currentFormFlowStepInstanceId!!.id.toString(),
            if (submission == null) null else jacksonObjectMapper().readTree(submission)
        )
    }

    private fun deployFormFlow(onComplete: String) {
        val formFlowJson = readFileAsString("/template/single_step_flow.json")
            .replace("PLACEHOLDER", onComplete)
        formFlowDeploymentService.deploy("single_step_flow", formFlowJson)
    }

    private fun linkFormFlowToUserTask() {
        formAssociationService.createFormAssociation(
            CreateFormAssociationRequest(
                "formflow-one-task-process",
                FormLinkRequest(
                    "do-something",
                    FormAssociationType.USER_TASK,
                    null,
                    "single_step_flow:latest",
                    null,
                    null
                )
            )
        )
    }

    private fun linkFormFlowToStartEvent(): ProcessLink {
        newProcessLinkService.createProcessLink(
            FormFlowProcessLinkCreateRequestDto(
                getProcessDefinitionId(),
                "start-event",
                ActivityTypeWithEventName.START_EVENT_START,
                "single_step_flow:latest"
            )
        )
        return newProcessLinkService.getProcessLinks(
            getProcessDefinitionId(),
            "start-event"
        )[0]
    }

    private fun getProcessDefinitionId(): String {
        return repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("formflow-one-task-process")
            .latestVersion()
            .singleResult()
            .id
    }

    private fun newDocumentAndStartProcess(): NewDocumentAndStartProcessResult {
        return processDocumentService.newDocumentAndStartProcess(
            NewDocumentAndStartProcessRequest(
                "formflow-one-task-process",
                NewDocumentRequest(
                    "profile",
                    Mapper.INSTANCE.get().readTree("{}")
                ).withTenantId("1")
            )
        )
    }

    private fun openTasks(processInstanceId: ProcessInstanceId): List<FormFlowInstance> {
        return taskService.createTaskQuery()
            .processInstanceId(processInstanceId.toString())
            .list()
            .asSequence()
            .map { processLinkService.openTask(UUID.fromString(it.id)) }
            .filter { it.properties is FormFlowTaskOpenResultProperties }
            .map { (it.properties as FormFlowTaskOpenResultProperties).formFlowInstanceId }
            .map { formFlowService.getInstanceById(FormFlowInstanceId(it)) }
            .toList()
    }
}
