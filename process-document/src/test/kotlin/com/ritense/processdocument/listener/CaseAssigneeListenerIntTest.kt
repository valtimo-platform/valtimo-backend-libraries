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

package com.ritense.processdocument.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byName
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import com.ritense.valtimo.service.CamundaTaskService
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals

@Transactional
class CaseAssigneeListenerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var caseDefinitionService: CaseDefinitionService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var taskService: CamundaTaskService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var testDocument: Document

    lateinit var testUser: ManageableUser

    lateinit var testUser2: ManageableUser

    @BeforeEach
    fun init() {
        testUser = ValtimoUserBuilder()
            .id(UUID.randomUUID().toString())
            .firstName("Test")
            .lastName("User")
            .email("test@valtimo.nl")
            .roles(listOf("ROLE_USER"))
            .build()

        testUser2 = ValtimoUserBuilder()
            .id(UUID.randomUUID().toString())
            .firstName("Test")
            .lastName("User 2")
            .email("test2@valtimo.nl")
            .roles(listOf("ROLE_USER"))
            .build()

        val documentJson =
            """
            {
                "street": "aStreet",
                "houseNumber": 1
            }
            """.trimIndent()

        testDocument = documentService.createDocument(
            NewDocumentRequest(
                "house", objectMapper.readTree(documentJson)
            )
        ).resultingDocument().orElseThrow()

        caseDefinitionService.updateCaseSettings(
            caseDefinitionName = "house",
            CaseSettingsDto(
                canHaveAssignee = true,
                autoAssignTasks = true
            )
        )
    }

    @Test
    fun `should set assignee when task is created and autoAssignTasks is on`() {

        whenever(userManagementService.findById(any())).thenReturn(testUser)

        documentService.assignUserToDocument(testDocument.id().id, testUser.id)
        val processInstance = runtimeService.startProcessInstanceByKey(
            "parent-process",
            testDocument.id().toString()
        )
        AuthorizationContext.runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                testDocument.id().id,
                "parent process"
            )
        }

        val task = AuthorizationContext.runWithoutAuthorization {
            taskService.findTask(byName("child process user task"))
        }
        assertEquals(task.assignee, testUser.email)
    }

    @Test
    fun `should do nothing when and task is created and autoAssignTasks is off`() {

        caseDefinitionService.updateCaseSettings(
            caseDefinitionName = "house",
            CaseSettingsDto(
                canHaveAssignee = true,
                autoAssignTasks = false
            )
        )

        whenever(userManagementService.findById(any())).thenReturn(testUser)

        documentService.assignUserToDocument(testDocument.id().id, testUser.id)
        val processInstance = runtimeService.startProcessInstanceByKey(
            "parent-process",
            testDocument.id().toString()
        )
        AuthorizationContext.runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                testDocument.id().id,
                "parent process"
            )
        }

        val task = AuthorizationContext.runWithoutAuthorization {
            taskService.findTask(byName("child process user task"))
        }

        assertNull(task.assignee)
    }

    @Test
    fun `should should update task assignee when document assignee is changed`() {

        whenever(userManagementService.findById(any())).thenReturn(testUser, testUser2)

        documentService.assignUserToDocument(testDocument.id().id, testUser.id)
        val processInstance = runtimeService.startProcessInstanceByKey(
            "parent-process",
            testDocument.id().toString()
        )
        AuthorizationContext.runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                testDocument.id().id,
                "parent process"
            )
        }

        documentService.assignUserToDocument(testDocument.id().id, testUser2.id)

        val updatedTask = AuthorizationContext.runWithoutAuthorization {
            taskService.findTask(byName("child process user task"))
        }
        assertEquals(updatedTask.assignee, testUser2.email)

    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [ADMIN])
    fun `should should remove task assignee when document assignee is removed`() {

        whenever(userManagementService.findById(any())).thenReturn(testUser)

        documentService.assignUserToDocument(testDocument.id().id, testUser.id)
        val processInstance = runtimeService.startProcessInstanceByKey(
            "parent-process",
            testDocument.id().toString()
        )
        AuthorizationContext.runWithoutAuthorization {
            processDocumentAssociationService.createProcessDocumentInstance(
                processInstance.id,
                testDocument.id().id,
                "parent process"
            )
        }

        documentService.unassignUserFromDocument(testDocument.id().id)

        val task = AuthorizationContext.runWithoutAuthorization {
            taskService.findTask(byName("child process user task"))
        }
        assertNull(task.assignee)
    }
}