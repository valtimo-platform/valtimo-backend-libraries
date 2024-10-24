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

package com.ritense.documentenapi.web.rest

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.documentenapi.BaseIntegrationTest
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertEquals

@Transactional
internal class ZgwDocumentTrefwoordResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var service: ZgwDocumentTrefwoordService

    @Autowired
    private lateinit var caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `test getTrefwoorden as a user`() {
        val caseDefinitionName = "TestDefinition"

        caseDefinitionSettingsRepository.save(CaseDefinitionSettings(caseDefinitionName))

        service.createTrefwoord(caseDefinitionName, "Trefwoord1")
        service.createTrefwoord(caseDefinitionName, "Trefwoord2")

        mockMvc.perform(get("/api/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord", caseDefinitionName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].caseDefinitionName").value("TestDefinition"))
            .andExpect(jsonPath("$[0].value").value("Trefwoord1"))
            .andExpect(jsonPath("$[1].caseDefinitionName").value("TestDefinition"))
            .andExpect(jsonPath("$[1].value").value("Trefwoord2"))
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test getTrefwoorden`() {
        val caseDefinitionName = "TestDefinition"

        caseDefinitionSettingsRepository.save(CaseDefinitionSettings(caseDefinitionName))

        service.createTrefwoord(caseDefinitionName, "Trefwoord1")
        service.createTrefwoord(caseDefinitionName, "Trefwoord2")

        mockMvc.perform(get("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord", caseDefinitionName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].caseDefinitionName").value("TestDefinition"))
            .andExpect(jsonPath("$.content[0].value").value("Trefwoord1"))
            .andExpect(jsonPath("$.content[1].caseDefinitionName").value("TestDefinition"))
            .andExpect(jsonPath("$.content[1].value").value("Trefwoord2"))
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test getTrefwoorden with search`() {
        val caseDefinitionName = "TestDefinition"

        caseDefinitionSettingsRepository.save(CaseDefinitionSettings(caseDefinitionName))

        service.createTrefwoord(caseDefinitionName, "test123")
        service.createTrefwoord(caseDefinitionName, "test456")

        mockMvc.perform(get("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord?search=test1", caseDefinitionName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].caseDefinitionName").value("TestDefinition"))
            .andExpect(jsonPath("$.content[0].value").value("test123"))
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test createTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        mockMvc.perform(post("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}", caseDefinitionName, trefwoord)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test deleteTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        mockMvc.perform(delete("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}", caseDefinitionName, trefwoord)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test deleteTrefwoorden`() {
        val caseDefinitionName = "TestDefinition"

        caseDefinitionSettingsRepository.save(CaseDefinitionSettings(caseDefinitionName))

        service.createTrefwoord(caseDefinitionName, "Trefwoord1")
        service.createTrefwoord(caseDefinitionName, "Trefwoord2")
        service.createTrefwoord(caseDefinitionName, "Trefwoord3")

        mockMvc.perform(delete("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord", caseDefinitionName)
            .contentType(MediaType.APPLICATION_JSON)
            .content("[\"Trefwoord1\", \"Trefwoord2\"]"))
            .andExpect(status().isNoContent)

        service.getTrefwoorden(caseDefinitionName).let {
            assertEquals(1, it.size)
            assertEquals("Trefwoord3", it[0].value)
        }
    }
}
