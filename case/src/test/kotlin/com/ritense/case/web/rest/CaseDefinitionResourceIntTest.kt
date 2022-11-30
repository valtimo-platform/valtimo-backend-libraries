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

package com.ritense.case.web.rest

import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.document.service.DocumentDefinitionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertEquals

class CaseDefinitionResourceIntTest: BaseIntegrationTest() {
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var repository: CaseDefinitionSettingsRepository

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun `should get case settings with default values`() {

        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"\$id\": \"resource-test-default.schema\",\n" +
            "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n")


        val caseDefinitionName = "resource-test-default"

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(false))
    }

    @Test
    fun `should update case settings`() {

        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"\$id\": \"resource-test-update.schema\",\n" +
            "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n")


        val caseDefinitionName = "resource-test-update"

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\"canHaveAssignee\": true}")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))

        val settingsInDatabase = repository.getById(caseDefinitionName)

        assertEquals(true, settingsInDatabase.canHaveAssignee)
        assertEquals(caseDefinitionName, settingsInDatabase.name)
    }

    @Test
    fun `should not update case settings property when it has not been submitted`() {
        val caseDefinitionName = "resource-test-empty"

        val settings = CaseDefinitionSettings(caseDefinitionName, true)
        repository.save(settings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{}")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))

        val settingsInDatabase = repository.getById(caseDefinitionName)

        assertEquals(true, settingsInDatabase.canHaveAssignee)
        assertEquals(caseDefinitionName, settingsInDatabase.name)
    }

    @Test
    fun `should return not found when getting settings for case that does not exist`() {
        val caseDefinitionName = "some-case-that-does-not-exist"

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `should return not found when updating settings for case that does not exist`() {
        val caseDefinitionName = "some-case-that-does-not-exist"

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\"canHaveAssignee\": true}")
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}