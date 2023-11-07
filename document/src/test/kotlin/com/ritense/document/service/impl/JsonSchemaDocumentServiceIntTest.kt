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
package com.ritense.document.service.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import com.ritense.document.event.DocumentCreated
import com.ritense.document.event.DocumentDeleted
import com.ritense.document.event.DocumentUpdated
import com.ritense.document.event.DocumentViewed
import com.ritense.document.service.JsonSchemaDocumentActionProvider
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.authentication.NamedUser
import com.ritense.valtimo.contract.json.Mapper
import java.util.UUID
import java.util.function.Supplier
import javax.transaction.Transactional
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
import org.springframework.security.test.context.support.WithMockUser

@Tag("integration")
@Transactional
internal class JsonSchemaDocumentServiceIntTest : BaseIntegrationTest() {
    lateinit var definition: JsonSchemaDocumentDefinition
    lateinit var originalDocument: Document
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

        val candidateUsers = documentService.getCandidateUsers(originalDocument.id())

        assertThat(candidateUsers).contains(ashaMiller)
        Mockito.verify(userManagementService, never()).findByRole(ADMIN)
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should get all candidate users for 'public' documents`() {

        val candidateUsers = documentService.getCandidateUsers(listOf(originalDocument.id()))

        assertThat(candidateUsers).contains(jamesVance)
        assertThat(candidateUsers).contains(ashaMiller)
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should get admin candidate users for multiple documents`() {
        val document = createDocument("""{"street": "Admin street"}""")

        val candidateUsers = documentService.getCandidateUsers(listOf(originalDocument.id(), document.id()))

        assertThat(candidateUsers).doesNotContain(jamesVance)
        assertThat(candidateUsers).contains(ashaMiller)
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should send outboxMessage when using findBy`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        documentService.findBy(document.id())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentViewed }
        assertThat(event.resultId).isEqualTo(document.id().toString())
        assertThat(event.result).isEqualTo(Mapper.INSTANCE.get().valueToTree(document))
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should send outboxMessage when using get`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        documentService.get(document.id().toString())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentViewed }
        assertThat(event.resultId).isEqualTo(document.id().toString())
        assertThat(event.result).isEqualTo(Mapper.INSTANCE.get().valueToTree(document))
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should send outboxMessage when deleting documents`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        documentService.removeDocuments(document.definitionId().name())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentDeleted && it.resultId == document.id().toString() }
        assertThat(event).isNotNull
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should send outboxMessage when using getDocumentBy`() {
        val document = createDocument("""{"street": "Admin street"}""")

        reset(outboxService)
        (documentService as JsonSchemaDocumentService).getDocumentBy(document.id())

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentViewed }
        assertThat(event.resultId).isEqualTo(document.id().toString())
        assertThat(event.result).isEqualTo(Mapper.INSTANCE.get().valueToTree(document))
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should send outboxMessage when creating document`() {
        reset(outboxService)

        val document = createDocument("""{"street": "Admin street"}""")

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentCreated }
        assertThat(event.resultId).isEqualTo(document.id().toString())
        assertThat(event.result).isEqualTo(Mapper.INSTANCE.get().valueToTree(document))
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = [ADMIN])
    fun `should send outboxMessage when updating document`() {
        val document = createDocument("""{"street": "Admin street"}""")
        val modifiedContent = jacksonObjectMapper().readTree("""{"street": "MODIFIED street"}""")
        val documentRequest = ModifyDocumentRequest.create(document, modifiedContent)
        reset(outboxService)

        val modifiedDocument = documentService.modifyDocument(documentRequest).resultingDocument().orElseThrow()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, atLeastOnce()).send(eventCapture.capture())
        val event = eventCapture.allValues.map { it.get() }
            .single { it is DocumentUpdated }
        assertThat(event.resultId).isEqualTo(document.id().toString())
        assertThat(event.result).isEqualTo(Mapper.INSTANCE.get().valueToTree(modifiedDocument))
    }

    private fun createDocument(content: String): Document {
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    definition().id().name(),
                    JsonDocumentContent(content).asJson()
                )
            )
        }.resultingDocument().orElseThrow()
    }

    companion object {
        private const val USER_ID = "a28994a3-31f9-4327-92a4-210c479d3055"
        private const val USERNAME = "john@ritense.com"
    }
}
