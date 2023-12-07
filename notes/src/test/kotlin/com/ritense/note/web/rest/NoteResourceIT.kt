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

package com.ritense.note.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.JsonPath
import com.ritense.audit.service.AuditService
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.Mapper
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.note.BaseIntegrationTest
import com.ritense.note.repository.NoteRepository
import com.ritense.note.service.NoteService
import com.ritense.note.web.rest.dto.NoteCreateRequestDto
import com.ritense.note.web.rest.dto.NoteUpdateRequestDto
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID

internal class NoteResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var noteService: NoteService

    @Autowired
    lateinit var noteRepository: NoteRepository

    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var auditService: AuditService

    lateinit var mockMvc: MockMvc
    lateinit var documentId: UUID

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()

        documentId = AuthorizationContext.runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(PROFILE_DOCUMENT_DEFINITION_NAME, Mapper.INSTANCE.get().createObjectNode())
            ).resultingDocument().get().id()!!.id
        }
        whenever(userManagementService.currentUser)
            .thenReturn(ValtimoUserBuilder().id("anId").firstName("aFirstName").lastName("aLastName").build())
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = [USER])
    fun `should create note`() {
        val note = NoteCreateRequestDto(content = "Test note")

        mockMvc.perform(
            post("/api/v1/document/{documentId}/note", documentId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(note))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.createdByUserId").value("anId"))
            .andExpect(jsonPath("$.createdByUserFullName").value("aFirstName aLastName"))
            .andExpect(jsonPath("$.createdDate").isNotEmpty)
            .andExpect(jsonPath("$.content").value("Test note"))
            .andExpect(jsonPath("$.documentId").value(documentId.toString()))
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = [USER])
    fun `should audit note creation`() {
        val note = NoteCreateRequestDto(content = "Test note")

        val responseBody = mockMvc.perform(
            post("/api/v1/document/{documentId}/note", documentId.toString())
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(note))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn().response

        val noteId = JsonPath.read<String>(responseBody.contentAsString, "$.id")
        val auditList = AuthorizationContext.runWithoutAuthorization {
            auditService.findByProperty("noteId", noteId, Pageable.unpaged()).toList()
        }
        auditList.size shouldBeExactly 1
        auditList[0].documentId shouldBe documentId
    }

    @Test
    @WithMockUser(TEST_USER, authorities = ["DENY"])
    fun `should not create note when user has no permission to the document`() {
        val note = NoteCreateRequestDto(content = "Test note")

        mockMvc.perform(
            post("/api/v1/document/{documentId}/note", documentId.toString())
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(note))
        )
            .andDo(print())
            // For some reason, the @ExceptionHandler is not picked up when using mockMvc
            .andExpect(status().is5xxServerError)
            .andExpect(jsonPath("$.detail").value("Unauthorized"))
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = [ADMIN])
    fun `should get notes`() {
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(documentId)

        val testContent = "body test"
        AuthorizationContext.runWithoutAuthorization { noteService.createNote(jsonSchemaDocumentId, testContent) }

        mockMvc.perform(
            get("/api/v1/document/{documentId}/note", documentId)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").isNotEmpty)
            .andExpect(jsonPath("$.content[0].createdByUserId").value("anId"))
            .andExpect(jsonPath("$.content[0].createdByUserFullName").value("aFirstName aLastName"))
            .andExpect(jsonPath("$.content[0].createdDate").isNotEmpty)
            .andExpect(jsonPath("$.content[0].content").value(testContent))
            .andExpect(jsonPath("$.content[0].documentId").value(documentId.toString()))
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = [USER])
    fun `should update note`() {
        val note = noteService.createNote(JsonSchemaDocumentId.existingId(documentId), "Test note")
        val noteUpdateRequestDto = NoteUpdateRequestDto(content = "Test note updated")

        mockMvc.perform(
            put("/api/v1/note/{noteId}", note.id)
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(noteUpdateRequestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("Test note updated"))
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = [USER])
    fun `should delete note`() {
        val note = noteService.createNote(JsonSchemaDocumentId.existingId(documentId), "Test note")

        mockMvc.perform(
            delete("/api/v1/note/{noteId}", note.id)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        assertThat(noteRepository.existsById(note.id)).isFalse
    }

    companion object {
        private const val TEST_USER = "user@valtimo.nl"
        private const val PROFILE_DOCUMENT_DEFINITION_NAME = "profile"
    }
}
