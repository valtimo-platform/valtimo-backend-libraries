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

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.JsonSchemaDocumentActionProvider
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.authentication.NamedUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import java.util.UUID
import javax.transaction.Transactional

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
