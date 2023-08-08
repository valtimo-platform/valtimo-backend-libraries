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
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
class ProcessDocumentsServiceIntTest : BaseIntegrationTest() {

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
    fun `should start process by process definition key`() {
        document = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            ).withTenantId("1")
        ).resultingDocument().orElseThrow()
        val processInstance = runtimeService.startProcessInstanceByKey(
            "parent-process",
            document.id().toString()
        )
        processDocumentAssociationService.createProcessDocumentInstance(
            processInstance.id,
            document.id().id,
            "parent process"
        )
        val task = taskService.createTaskQuery().taskName("child process user task").singleResult()
        assertNotNull(task)
        val startedProcessId = task.processInstanceId
        val associatedProcessDocuments =
            processDocumentInstanceRepository.findAllByDocumentId(JsonSchemaDocumentId.existingId(document.id().id))
        val resultProcessInstance = camundaProcessService.findProcessInstanceById(startedProcessId).get()
        assertEquals(document.id().toString(), resultProcessInstance.businessKey)
        assertEquals(associatedProcessDocuments.size, 2)
        assertNotNull(associatedProcessDocuments.firstOrNull { it.processName().equals("parent process") })
        assertNotNull(associatedProcessDocuments.firstOrNull { it.processName().equals("child process") })
        assertEquals(
            document.id(), associatedProcessDocuments.first {
            it.processName().equals("parent process")
        }.id!!.documentId()
        )
        assertEquals(
            document.id(), associatedProcessDocuments.first {
            it.processName().equals("child process")
        }.id!!.documentId()
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun `should fail to start process with non existing process definition key`() {
        document = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            ).withTenantId("1")
        ).resultingDocument().orElseThrow()
        val exception = assertThrows<ProcessEngineException> {
            runtimeService.startProcessInstanceByKey(
                "parent-process-with-non-existing-key",
                document.id().toString()
            )
        }
        assertEquals(
            "java.lang.IllegalStateException: No process definition found with key: 'non-existing-key'",
            exception.cause?.message
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun `should fail to start process with non existing document`() {
        val uuid = UUID.randomUUID().toString()
        val exception = assertThrows<ProcessEngineException> {
            runtimeService.startProcessInstanceByKey(
                "parent-process",
                uuid
            )
        }
        assertEquals(
            "com.ritense.document.exception.DocumentNotFoundException: No Document found with id $uuid",
            exception.cause?.message
        )
    }

}