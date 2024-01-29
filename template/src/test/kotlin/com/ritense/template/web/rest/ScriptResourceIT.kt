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

package com.ritense.template.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.template.BaseIntegrationTest
import com.ritense.template.domain.ValtimoTemplate
import com.ritense.template.repository.TemplateRepository
import com.ritense.template.service.TemplateService
import com.ritense.template.web.rest.dto.DeleteTemplateRequest
import com.ritense.template.web.rest.dto.TemplateDto
import com.ritense.template.web.rest.dto.TemplateMetadataDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
internal class ValtimoTemplateManagementResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var templateService: TemplateService

    @Autowired
    lateinit var templateRepository: TemplateRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should get templates`() {
        templateService.createTemplate(ValtimoTemplate(key = "my-template"))
        mockMvc.perform(
            get("/api/management/v1/template")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.key == 'my-template')].key").value("my-template"))
    }

    @Test
    fun `should create template`() {
        val template = TemplateMetadataDto(key = "my-template")

        mockMvc.perform(
            post("/api/management/v1/template")
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(template))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("my-template"))
    }

    @Test
    fun `should delete templates`() {
        templateService.createTemplate(ValtimoTemplate(key = "my-template"))
        val template = DeleteTemplateRequest(templates = listOf("my-template"))

        mockMvc.perform(
            delete("/api/management/v1/template")
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(template))
        )
            .andDo(print())
            .andExpect(status().isOk)
        assertThat(templateRepository.findById("my-template")).isEmpty
    }

    @Test
    fun `should get template content`() {
        val template = ValtimoTemplate(key = "my-template")
        template.content = "var1 + var2"
        templateService.createTemplate(template)

        mockMvc.perform(
            get("/api/management/v1/template/{key}/content", "my-template")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("var1 + var2"))
    }

    @Test
    fun `should update template content`() {
        val template = ValtimoTemplate(key = "my-template")
        template.content = "var1 + var2"
        templateService.createTemplate(template)

        mockMvc.perform(
            put("/api/management/v1/template/{key}/content", "my-template")
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(TemplateDto("var1 + var2 + 100")))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("var1 + var2 + 100"))
    }
}
