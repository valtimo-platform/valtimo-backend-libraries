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

package com.ritense.script.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.script.BaseIntegrationTest
import com.ritense.script.domain.Script
import com.ritense.script.repository.ScriptRepository
import com.ritense.script.service.ScriptService
import com.ritense.script.web.rest.dto.DeleteScriptRequest
import com.ritense.script.web.rest.dto.ScriptContentDto
import com.ritense.script.web.rest.dto.ScriptDto
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
internal class ScriptManagementResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var scriptService: ScriptService

    @Autowired
    lateinit var scriptRepository: ScriptRepository

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
    fun `should get scripts`() {
        scriptService.createScript(Script(key = "my-script"))
        mockMvc.perform(
            get("/api/management/v1/script")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.key == 'my-script')].key").value("my-script"))
    }

    @Test
    fun `should create script`() {
        val script = ScriptDto(key = "my-script")

        mockMvc.perform(
            post("/api/management/v1/script")
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(script))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("my-script"))
    }

    @Test
    fun `should delete scripts`() {
        scriptService.createScript(Script(key = "my-script"))
        val script = DeleteScriptRequest(scripts = listOf("my-script"))

        mockMvc.perform(
            delete("/api/management/v1/script")
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(script))
        )
            .andDo(print())
            .andExpect(status().isOk)
        assertThat(scriptRepository.findById("my-script")).isEmpty
    }

    @Test
    fun `should get script content`() {
        val script = Script(key = "my-script")
        script.content = "var1 + var2"
        scriptService.createScript(script)

        mockMvc.perform(
            get("/api/management/v1/script/{key}/content", "my-script")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("var1 + var2"))
    }

    @Test
    fun `should update script content`() {
        val script = Script(key = "my-script")
        script.content = "var1 + var2"
        scriptService.createScript(script)

        mockMvc.perform(
            put("/api/management/v1/script/{key}/content", "my-script")
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(ScriptContentDto("var1 + var2 + 100")))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("var1 + var2 + 100"))
    }
}
