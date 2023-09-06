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

package com.ritense.valtimo.formflow.web.rest

import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.configuration.FormFlowStepType
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.service.FormFlowService
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class ProcessLinkFormFlowDefinitionResourceTest {
    lateinit var mockMvc: MockMvc
    lateinit var resource: ProcessLinkFormFlowDefinitionResource
    lateinit var service: FormFlowService

    @BeforeEach
    fun init() {
        service = mock()
        resource = ProcessLinkFormFlowDefinitionResource(service)
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build()
    }

    @Test
    fun `getFormLinkOptions returns form flow definition with latest`() {
        val step = FormFlowStep(FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
        )
        val definition = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key"), "step1", mutableSetOf(step))
        whenever(service.getFormFlowDefinitions()).thenReturn(listOf(definition))
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/api/v1/form-flow/definition")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Any>(2)))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder("key (v1)", "key (latest)")))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder("key:1", "key:latest")))
    }

    @Test
    fun `getFormLinkOptions returns multiple versions of form flow definition with only one latest`() {
        val step = FormFlowStep(FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition")))
        val formFlowDefinitionId = FormFlowDefinitionId.newId("key")
        val definitionVersion1 = FormFlowDefinition(
            id = formFlowDefinitionId, "step1", mutableSetOf(step))

        val definitionVersion2 = FormFlowDefinition(
            id = FormFlowDefinitionId.nextVersion(formFlowDefinitionId) , "step1", mutableSetOf(step))

        whenever(service.getFormFlowDefinitions()).thenReturn(listOf(definitionVersion1, definitionVersion2))
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/api/v1/form-flow/definition")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Any>(3)))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder("key (v1)", "key (v2)", "key (latest)")))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder("key:1", "key:2", "key:latest")))
    }

    @Test
    fun `getFormLinkOptions returns form flow definitions with one latest per unique key`() {
        val step = FormFlowStep(FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition")))
        val definitionVersion1 = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key"), "step1", mutableSetOf(step))

        val definitionVersion2 = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("another-key") , "step1", mutableSetOf(step))

        whenever(service.getFormFlowDefinitions()).thenReturn(listOf(definitionVersion1, definitionVersion2))
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/api/v1/form-flow/definition")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Any>(4)))
            .andExpect(jsonPath("$[*].name",
                containsInAnyOrder("key (v1)", "another-key (v1)", "key (latest)", "another-key (latest)")))
            .andExpect(jsonPath("$[*].id",
                containsInAnyOrder("key:1", "another-key:1", "key:latest", "another-key:latest")))
    }
}