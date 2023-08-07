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
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.formflow.BaseIntegrationTest
import com.ritense.valtimo.formflow.FormFlowTaskOpenResultProperties
import com.ritense.valtimo.formflow.web.rest.FormFlowResource
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class ValtimoFormFlowIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var formAssociationService: FormAssociationService

    @Autowired
    lateinit var processLinkService: ProcessLinkService

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
                "one-task-process",
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

    private fun newDocumentAndStartProcess(): NewDocumentAndStartProcessResult {
        return processDocumentService.newDocumentAndStartProcess(
            NewDocumentAndStartProcessRequest(
                "one-task-process",
                NewDocumentRequest(
                    "profile",
                    Mapper.INSTANCE.get().readTree("{}"),
                    "1"
                )
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
