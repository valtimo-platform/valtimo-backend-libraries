/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import com.ritense.document.domain.impl.Mapper
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.note.BaseIntegrationTest
import com.ritense.note.web.rest.dto.NoteCreateRequestDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

internal class NoteResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var documentService: DocumentService

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    @WithMockUser(TEST_USER)
    fun `should create note`() {
        val documentId = documentService.createDocument(
            NewDocumentRequest(PROFILE_DOCUMENT_DEFINITION_NAME, Mapper.INSTANCE.get().createObjectNode())
        ).resultingDocument().get().id()!!.id.toString()
        val note = NoteCreateRequestDto(content = "Test note")

        mockMvc.perform(
            post("/api/document/{documentId}/note", documentId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(note))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.createdBy").value(TEST_USER))
            .andExpect(jsonPath("$.createdDate").isNotEmpty)
            .andExpect(jsonPath("$.content").value("Test note"))
            .andExpect(jsonPath("$.documentId").value(documentId))
    }

    companion object {
        private const val TEST_USER = "user@valtimo.nl"
        private const val PROFILE_DOCUMENT_DEFINITION_NAME = "profile"
    }
}
