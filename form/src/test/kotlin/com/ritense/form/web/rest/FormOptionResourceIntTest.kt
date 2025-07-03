/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.form.web.rest

import com.ritense.form.BaseIntegrationTest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

class FormOptionResourceIntTest(
    @Autowired
    private val resource: FormOptionResource
): BaseIntegrationTest() {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        formDefinitionRepository.deleteAll()
        mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()
    }

    @Test
    @Transactional
    fun `should get form definitions that are not part of a case definition ordered by name`() {
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form2", null))
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form1", null))
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form3", null))
        // this option should not be found
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form4"))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/management/v1/form-option")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize<Any>(3)))
            .andExpect(jsonPath("$[0].name", equalTo("form1")))
            .andExpect(jsonPath("$[1].name", equalTo("form2")))
            .andExpect(jsonPath("$[2].name", equalTo("form3")))
    }

    @Test
    @Transactional
    fun `should get form definitions that are part of a case definition ordered by name`() {
        val caseDefinitionId = CaseDefinitionId.of("some-key", "1.2.3")
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form2", caseDefinitionId))
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form1", caseDefinitionId))
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form3", caseDefinitionId))
        // this option should not be found
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form4"))

        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-option",
                "some-key",
                "1.2.3"
            )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize<Any>(3)))
            .andExpect(jsonPath("$[0].name", equalTo("form1")))
            .andExpect(jsonPath("$[1].name", equalTo("form2")))
            .andExpect(jsonPath("$[2].name", equalTo("form3")))
    }
}