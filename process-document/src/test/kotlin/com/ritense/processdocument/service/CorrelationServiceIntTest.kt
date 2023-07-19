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

package com.ritense.processdocument.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Transactional
class CorrelationServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var processDocumentInstanceRepository: ProcessDocumentInstanceRepository

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var camundaProcessService: CamundaProcessService

    lateinit var documentJson: String
    lateinit var document: Document

    @BeforeEach
    fun init() {
        documentJson =
            """
            {
                "street": "aStreet",
                "houseNumber": 1
            }
            """.trimIndent()
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun `should correlate start event`() {
        val variables = HashMap<String, Any>()
        variables["variable"] = "start-event-test"
        document = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            )
        ).resultingDocument().orElseThrow()
        val processInstance = runtimeService.startProcessInstanceByKey(
            "start-correlation-test-id",
            document.id().toString(),
            variables
        )
        runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                document.id().id,
                "start-correlation-test-process"
            )
        }
        val task = taskService.createTaskQuery().taskName("message-start-event-user-task").singleResult()
        assertNotNull(task)
        val startedProcessId = task.processInstanceId
        val associatedProcessDocuments =
            processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val resultProcessInstance = runWithoutAuthorization {
            camundaProcessService.findProcessInstanceById(startedProcessId).get()
        }
        assertEquals(document.id().toString(),resultProcessInstance.businessKey)
        assertEquals(associatedProcessDocuments.size,2)
        assertNotNull(associatedProcessDocuments.firstOrNull { it.processName().equals("start-correlation-test-process")})
        assertNotNull(associatedProcessDocuments.firstOrNull { it.processName().equals("message-start-event-name")})
        assertEquals(document.id(), associatedProcessDocuments.first {
            it.processName().equals("start-correlation-test-process")
        }.id!!.documentId()
        )
        assertEquals(document.id(), associatedProcessDocuments.first {
            it.processName().equals("message-start-event-name")
        }.id!!.documentId()
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun `should correlate only the process that matches provided business key`() {
        val variables = HashMap<String, Any>()
        variables["variable"] = "multiple-intermediate-catch-event-test-with-business-key"

        document = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            )
        ).resultingDocument().orElseThrow()
        variables["businessKey"] = document.id()
        val documentTwo = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            )
        ).resultingDocument().orElseThrow()
        runtimeService.startProcessInstanceByKey(
            "intermediate-catch-event-sample-one-id",
            document.id().toString(),
            emptyMap()
        )
        var taskOne = taskService.createTaskQuery().taskName("intermediate-catch-event-1-user-task").singleResult()
        assertNull(taskOne)
        runtimeService.startProcessInstanceByKey(
            "intermediate-catch-event-sample-two-id",
            documentTwo.id().toString(),
            emptyMap()
        )
        var taskTwo = taskService.createTaskQuery().taskName("intermediate-catch-event-2-user-task").singleResult()
        assertNull(taskTwo)

        val processInstance = runtimeService.startProcessInstanceByKey(
            "start-correlation-test-id",
            document.id().toString(),
            variables
        )

        runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                document.id().id,
                "start-correlation-test-process"
            )
        }
        taskOne = taskService.createTaskQuery().taskName("intermediate-catch-event-1-user-task").singleResult()
        assertNotNull(taskOne)
        taskTwo = taskService.createTaskQuery().taskName("intermediate-catch-event-2-user-task").singleResult()
        assertNull(taskTwo)
        val startedProcessOneId = taskOne.processInstanceId
        val resultProcessOneInstance = runWithoutAuthorization {
            camundaProcessService.findProcessInstanceById(startedProcessOneId).get()
        }

        val associatedProcessDocumentsForDocumentOne =
            processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val associatedProcessDocumentsForDocumentTwo =
            processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(JsonSchemaDocumentId.existingId(documentTwo.id().id))
        assertEquals(document.id().toString(),resultProcessOneInstance.businessKey)
        assertEquals(associatedProcessDocumentsForDocumentOne.size,2)
        assertNotNull(associatedProcessDocumentsForDocumentOne.firstOrNull { it.processName().equals("start-correlation-test-process")})
        assertNotNull(associatedProcessDocumentsForDocumentOne.firstOrNull { it.processName().equals("intermediate-catch-event-sample-one")})
        assertNull(associatedProcessDocumentsForDocumentOne.firstOrNull {it.processName().equals("intermediate-catch-event-sample-two") })
        assertNull(associatedProcessDocumentsForDocumentTwo.firstOrNull {it.processName().equals("intermediate-catch-event-sample-two") })
        assertEquals(document.id(), associatedProcessDocumentsForDocumentOne.first {
            it.processName().equals("start-correlation-test-process")
        }.id!!.documentId()
        )
        assertEquals(document.id(),associatedProcessDocumentsForDocumentOne.first {
            it.processName().equals("intermediate-catch-event-sample-one")
        }.id!!.documentId()
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun `should correlate event to a process definition key`() {
        val variables = HashMap<String, Any>()
        variables["variable"] = "process-definition-start-event-test"
        document = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            )
        ).resultingDocument().orElseThrow()
        val processInstance = runtimeService.startProcessInstanceByKey(
            "start-correlation-test-id",
            document.id().toString(),
            variables
        )
        runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                document.id().id,
                "start-correlation-test-process"
            )
        }
        val task = taskService.createTaskQuery().taskName("target-process-definition-user-task").singleResult()
        assertNotNull(task)
        val startedProcessId = task.processInstanceId
        val associatedProcessDocuments =
            processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val resultProcessInstance = runWithoutAuthorization {
            camundaProcessService.findProcessInstanceById(startedProcessId).get()
        }
        assertEquals(document.id().toString(),resultProcessInstance.businessKey)
        assertEquals(associatedProcessDocuments.size,2)
        assertNotNull(associatedProcessDocuments.firstOrNull { it.processName().equals("start-correlation-test-process")})
        assertNotNull(associatedProcessDocuments.firstOrNull { it.processName().equals("targetProcessDefinitionName")})
        assertEquals(document.id(), associatedProcessDocuments.first {
            it.processName().equals("start-correlation-test-process")
        }.id!!.documentId()
        )
        assertEquals(document.id(), associatedProcessDocuments.first {
            it.processName().equals("targetProcessDefinitionName")
        }.id!!.documentId()
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun `should correlate a process for message ref with business key`() {
        val variables = HashMap<String, Any>()
        variables["variable"] = "intermediate-catch-event-test-with-business-key"
        document = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            )
        ).resultingDocument().orElseThrow()
        variables["businessKey"] = document.id()
        runtimeService.startProcessInstanceByKey(
            "intermediate-catch-event-sample-one-id",
            document.id().toString(),
            emptyMap()
        )
        var taskOne = taskService.createTaskQuery().taskName("intermediate-catch-event-1-user-task").singleResult()
        assertNull(taskOne)

        val processInstance = runtimeService.startProcessInstanceByKey(
            "start-correlation-test-id",
            document.id().toString(),
            variables
        )
        runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                document.id().id,
                "start-correlation-test-process"
            )
        }
        taskOne = taskService.createTaskQuery().taskName("intermediate-catch-event-1-user-task").singleResult()
        assertNotNull(taskOne)
        val startedProcessOneId = taskOne.processInstanceId
        val resultProcessOneInstance = runWithoutAuthorization {
            camundaProcessService.findProcessInstanceById(startedProcessOneId).get()
        }
        val associatedProcessDocumentsForDocumentOne =
            processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        assertEquals(resultProcessOneInstance.businessKey,document.id().toString())
        assertEquals(2,associatedProcessDocumentsForDocumentOne.size)
        assertNotNull(associatedProcessDocumentsForDocumentOne.firstOrNull { it.processName().equals("start-correlation-test-process")})
        assertNotNull(associatedProcessDocumentsForDocumentOne.firstOrNull { it.processName().equals("intermediate-catch-event-sample-one")})
        assertNull(associatedProcessDocumentsForDocumentOne.firstOrNull {it.processName().equals("intermediate-catch-event-sample-two") })
        assertEquals(document.id(), associatedProcessDocumentsForDocumentOne.first {
            it.processName().equals("start-correlation-test-process")
        }.id!!.documentId()
        )
        assertEquals(document.id(),associatedProcessDocumentsForDocumentOne.first {
            it.processName().equals("intermediate-catch-event-sample-one")
        }.id!!.documentId()
        )
    }

    @AfterEach
    fun destroy() {
        val tasks = taskService.createTaskQuery().list()
        tasks.forEach(Consumer { task: Task ->
            taskService.complete(
                task.id
            )
        })
    }
}