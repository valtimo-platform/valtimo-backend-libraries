package com.ritense.processdocument.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
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
        processDocumentAssociationService.createProcessDocumentInstance(
            processInstance.id,
            document.id().id,
            "start-correlation-test-process"
        )
        val task = taskService.createTaskQuery().taskName("message-start-event-user-task").singleResult()
        assertNotNull(task)
        val startedProcessId = task.processInstanceId
        val associatedProcessDocuments =
            processDocumentInstanceRepository.findAllByDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val resultProcessInstance = camundaProcessService.findProcessInstanceById(startedProcessId).get()
        assertEquals(resultProcessInstance.businessKey,document.id().toString())
        assertEquals(2,associatedProcessDocuments.size)
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
        variables["variable"] = "intermediate-catch-event-test-with-business-key"

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
        processDocumentAssociationService.createProcessDocumentInstance(
            processInstance.id,
            document.id().id,
            "start-correlation-test-process"
        )
        taskOne = taskService.createTaskQuery().taskName("intermediate-catch-event-1-user-task").singleResult()
        assertNotNull(taskOne)
        taskTwo = taskService.createTaskQuery().taskName("intermediate-catch-event-2-user-task").singleResult()
        assertNull(taskTwo)
        val startedProcessOneId = taskOne.processInstanceId
        val resultProcessOneInstance = camundaProcessService.findProcessInstanceById(startedProcessOneId).get()

        val associatedProcessDocumentsForDocumentOne =
            processDocumentInstanceRepository.findAllByDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val associatedProcessDocumentsForDocumentTwo =
            processDocumentInstanceRepository.findAllByDocumentId(JsonSchemaDocumentId.existingId(documentTwo.id().id))
        assertEquals(resultProcessOneInstance.businessKey,document.id().toString())
        assertEquals(2,associatedProcessDocumentsForDocumentOne.size)
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
        processDocumentAssociationService.createProcessDocumentInstance(
            processInstance.id,
            document.id().id,
            "start-correlation-test-process"
        )
        val task = taskService.createTaskQuery().taskName("target-process-definition-user-task").singleResult()
        assertNotNull(task)
        val startedProcessId = task.processInstanceId
        val associatedProcessDocuments =
            processDocumentInstanceRepository.findAllByDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val resultProcessInstance = camundaProcessService.findProcessInstanceById(startedProcessId).get()
        assertEquals(resultProcessInstance.businessKey,document.id().toString())
        assertEquals(2,associatedProcessDocuments.size)
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
    fun `should correlate all process for message ref with no business key`() {
        val variables = HashMap<String, Any>()
        variables["variable"] = "intermediate-catch-event-test-with-no-business-key"
        document = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            )
        ).resultingDocument().orElseThrow()
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
        processDocumentAssociationService.createProcessDocumentInstance(
            processInstance.id,
            document.id().id,
            "start-correlation-test-process"
        )
        taskOne = taskService.createTaskQuery().taskName("intermediate-catch-event-1-user-task").singleResult()
        assertNotNull(taskOne)
        taskTwo = taskService.createTaskQuery().taskName("intermediate-catch-event-2-user-task").singleResult()
        assertNotNull(taskTwo)
        val startedProcessOneId = taskOne.processInstanceId
        val resultProcessOneInstance = camundaProcessService.findProcessInstanceById(startedProcessOneId).get()
        val startedProcessTwoId = taskTwo.processInstanceId
        val resultProcessTwoInstance = camundaProcessService.findProcessInstanceById(startedProcessTwoId).get()

        val associatedProcessDocumentsForDocumentOne =
            processDocumentInstanceRepository.findAllByDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val associatedProcessDocumentsForDocumentTwo =
            processDocumentInstanceRepository.findAllByDocumentId(JsonSchemaDocumentId.existingId(documentTwo.id().id))
        assertEquals(resultProcessOneInstance.businessKey,document.id().toString())
        assertEquals(resultProcessTwoInstance.businessKey,documentTwo.id().toString())
        assertEquals(2,associatedProcessDocumentsForDocumentOne.size)
        assertEquals(1,associatedProcessDocumentsForDocumentTwo.size)
        assertNotNull(associatedProcessDocumentsForDocumentOne.firstOrNull { it.processName().equals("start-correlation-test-process")})
        assertNotNull(associatedProcessDocumentsForDocumentOne.firstOrNull { it.processName().equals("intermediate-catch-event-sample-one")})
        assertNull(associatedProcessDocumentsForDocumentOne.firstOrNull {it.processName().equals("intermediate-catch-event-sample-two") })
        assertNotNull(associatedProcessDocumentsForDocumentTwo.firstOrNull {it.processName().equals("intermediate-catch-event-sample-two") })
        assertEquals(document.id(), associatedProcessDocumentsForDocumentOne.first {
            it.processName().equals("start-correlation-test-process")
        }.id!!.documentId()
        )
        assertEquals(document.id(),associatedProcessDocumentsForDocumentOne.first {
            it.processName().equals("intermediate-catch-event-sample-one")
        }.id!!.documentId()
        )
        assertEquals(documentTwo.id(),associatedProcessDocumentsForDocumentTwo.first {
            it.processName().equals("intermediate-catch-event-sample-two")
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