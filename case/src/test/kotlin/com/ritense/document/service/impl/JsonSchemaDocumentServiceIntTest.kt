/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
package com.ritense.document.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.event.DocumentAssigned
import com.ritense.document.event.DocumentCreated
import com.ritense.document.event.DocumentDeleted
import com.ritense.document.event.DocumentStatusChanged
import com.ritense.document.event.DocumentUnassigned
import com.ritense.document.event.DocumentUpdated
import com.ritense.document.event.DocumentViewed
import com.ritense.document.event.DocumentsListed
import com.ritense.document.service.JsonSchemaDocumentActionProvider
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.authentication.NamedUser
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.function.Supplier

@Tag("integration")
@Transactional
internal class JsonSchemaDocumentServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var definition: JsonSchemaDocumentDefinition
    lateinit var originalDocument: JsonSchemaDocument
    lateinit var ashaMiller: NamedUser
    lateinit var jamesVance: NamedUser

    @BeforeEach
    fun beforeEach() {
        definition = definition()
        originalDocument = createDocument("""{"street": "Funenpark"}""")

        ashaMiller = NamedUser("1", "Asha", "Miller")
        jamesVance = NamedUser("2", "James ", "Vance")

        whenever(userManagementService.findNamedUserByRoles(setOf(ADMIN, FULL_ACCESS_ROLE)))
            .thenReturn(listOf(ashaMiller))

        whenever(userManagementService.findNamedUserByRoles(setOf(USER, ADMIN, FULL_ACCESS_ROLE)))
            .thenReturn(listOf(ashaMiller, jamesVance))

        whenever(userManagementService.findNamedUserByRoles(setOf(USER)))
            .thenReturn(listOf(ashaMiller, jamesVance))

        val admin = ValtimoUser()
        admin.id = USERNAME
        admin.username = USERNAME
        admin.roles = listOf(USER, ADMIN)
        whenever(userManagementService.currentUser).thenReturn(admin)
        whenever(userManagementService.findByUserIdentifier(USERNAME)).thenReturn(admin)
        whenever(userManagementService.findById(USERNAME)).thenReturn(admin)
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should get candidate users for document assignment`() {
        val adminRole = roleRepository.findByKey(ADMIN)!!
        val userRole = roleRepository.findByKey(USER)!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.ASSIGN,
                ConditionContainer(),
                adminRole
            ),
            Permission(
                UUID.randomUUID(),
                JsonSchemaDocument::class.java,
                JsonSchemaDocumentActionProvider.ASSIGNABLE,
                ConditionContainer(),
                userRole
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val candidateUsers = documentService.getCandidateUsers(originalDocument.id)

        assertThat(candidateUsers).contains(ashaMiller)
        Mockito.verify(userManagementService, never()).findByRole(ADMIN)
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should get all candidate users for 'public' documents`() {

        val candidateUsers = documentService.getCandidateUsers(listOf(originalDocument.id))

        assertThat(candidateUsers).contains(jamesVance)
        assertThat(candidateUsers).contains(ashaMiller)
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should set document status and send outbox event`() {
        val document = createDocument("""{"street": "Admin street"}""")
        assertThat(document.internalStatus()).isNull()

        reset(outboxService)

        val newStatusKey = "started"
        documentService.setInternalStatus(document.id, newStatusKey)

        //Assert change
        val modifiedDocument = documentService.findBy(document.id).get()
        assertThat(modifiedDocument.internalStatus()).isEqualTo(newStatusKey)

        //Assert outbox event
        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentStatusChanged }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
        assertThat(event.result?.get("internalStatus")?.asText()).isEqualTo(newStatusKey)
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should set document status to null and send outbox event`() {
        var document: Document = createDocument("""{"street": "Admin street"}""")
        documentService.setInternalStatus(document.id(), "started")
        document = documentService.findBy(document.id()).get()
        assertThat(document.internalStatus()).isNotNull()

        reset(outboxService)

        val newStatusKey: String? = null
        documentService.setInternalStatus(document.id(), newStatusKey)

        //Assert change
        val modifiedDocument = documentService.findBy(document.id()).get()
        assertThat(modifiedDocument.internalStatus()).isEqualTo(newStatusKey)

        //Assert outbox event
        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentStatusChanged }
        assertThat(event.resultId).isEqualTo(document.id()!!.toString())
        assertThat(event.result?.get("internalStatus")?.isNull).isTrue()
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should get admin candidate users for multiple documents`() {
        val document = createDocument("""{"street": "Admin street"}""")

        val candidateUsers = documentService.getCandidateUsers(listOf(originalDocument.id, document.id))

        assertThat(candidateUsers).doesNotContain(jamesVance)
        assertThat(candidateUsers).contains(ashaMiller)
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when using findBy`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        documentService.findBy(document.id)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentViewed }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when using get`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        documentService.get(document.id!!.toString())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentViewed }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when deleting documents`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        documentService.removeDocuments(document.definitionId().name())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentDeleted && it.resultId == document.id!!.toString() }
        assertThat(event).isNotNull
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when using getDocumentBy`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        (documentService as JsonSchemaDocumentService).getDocumentBy(document.id)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentViewed }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage on getAll`() {
        val documents = IntRange(0, 1).map {
            createDocument("""{"street": "Admin street"}""")
        }

        reset(outboxService)

        documentService.getAll(Pageable.unpaged())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single() { it is DocumentsListed }
        val result = objectMapper.writeValueAsString(event.result)
        documents.forEach { document ->
            assertThat(result).contains("\"${document.id}\"")
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage on getAllByDocumentDefinitionName`() {
        val documents = IntRange(0, 1).map {
            createDocument("""{"street": "Admin street"}""")
        }

        reset(outboxService)

        documentService.getAllByDocumentDefinitionName(Pageable.unpaged(), definition().id().name())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single() { it is DocumentsListed }
        val result = objectMapper.writeValueAsString(event.result)
        documents.forEach { document ->
            assertThat(result).contains("\"${document.id}\"")
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when creating document`() {
        reset(outboxService)

        val document = createDocument("""{"street": "Admin street"}""")

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentCreated }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when updating document`() {
        val document = createDocument("""{"street": "Admin street"}""")
        val modifiedContent = objectMapper.readTree("""{"street": "MODIFIED street"}""")
        val documentRequest = ModifyDocumentRequest.create(document, modifiedContent)
        reset(outboxService)

        val modifiedDocument = documentService.modifyDocument(documentRequest).resultingDocument().orElseThrow()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentUpdated }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(modifiedDocument))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage on assignUserToDocument`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)

        documentService.assignUserToDocument(document.id!!.id, USERNAME)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentAssigned }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when claimed`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)

        documentService.claim(document.id!!.id)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentAssigned }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage on assignUserToDocuments`() {
        val documents = IntRange(0, 1).map {
            createDocument("""{"street": "Admin street"}""")
        }

        reset(outboxService)

        documentService.assignUserToDocuments(documents.map { it.id().id }, USERNAME)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val events = eventCapture.allValues.map { it.get() }
            .filterIsInstance<DocumentAssigned>()
        documents.forEach { document ->
            val event = events.single { it.resultId == document.id!!.toString() }
            assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
        }
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should send outboxMessage when unassigned`() {
        val document = createDocument("""{"street": "Admin street"}""")
        documentService.claim(document.id!!.id)

        reset(outboxService)

        documentService.unassignUserFromDocument(document.id!!.id)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentUnassigned }
        assertThat(event.resultId).isEqualTo(document.id!!.toString())
        assertThat(event.result).isEqualTo(objectMapper.valueToTree(document))
    }

    private fun createDocument(content: String): JsonSchemaDocument {
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    definition().id().name(),
                    JsonDocumentContent(content).asJson()
                )
            )
        }.resultingDocument().orElseThrow() as JsonSchemaDocument
    }

    companion object {
        private const val USER_ID = "a28994a3-31f9-4327-92a4-210c479d3055"
        private const val USERNAME = "john@ritense.com"
    }
}
