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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.configuration.FormFlowStepType
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.formflow.BaseTest
import com.ritense.valtimo.formflow.handler.FormTypeProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class FormFlowResourceTest : BaseTest() {
    lateinit var mockMvc: MockMvc
    lateinit var formFlowResource: FormFlowResource
    lateinit var formFlowService: FormFlowService
    lateinit var formFlowInstance: FormFlowInstance
    lateinit var formFlowInstanceId: FormFlowInstanceId
    lateinit var stepInstance: FormFlowStepInstance
    lateinit var stepInstanceId: FormFlowStepInstanceId

    @BeforeEach
    fun setUp() {
        formFlowService = mock()
        whenever(formFlowService.getTypeProperties(any())).thenReturn(
            FormTypeProperties(
                jacksonObjectMapper().readTree(
                    readFileAsString("/config/form/user-task-lening-aanvragen.json")
                )
            )
        )

        val step1 = FormFlowStep(
            FormFlowStepId("step1"),
            type = FormFlowStepType("form", FormStepTypeProperties("first-form-definition"))
        )

        formFlowInstanceId = FormFlowInstanceId.newId()
        formFlowInstance = mock()
        whenever(formFlowInstance.id).thenReturn(formFlowInstanceId)
        whenever(formFlowService.getByInstanceIdIfExists(formFlowInstance.id)).thenReturn(formFlowInstance)
        formFlowResource = FormFlowResource(formFlowService)

        stepInstanceId = FormFlowStepInstanceId.newId()
        stepInstance = mock()

        whenever(formFlowInstance.getCurrentStep()).thenReturn(stepInstance)
        whenever(stepInstance.id).thenReturn(stepInstanceId)
        whenever(stepInstance.definition).thenReturn(step1)

        mockMvc = MockMvcBuilders.standaloneSetup(formFlowResource).build()
    }

    @Test
    fun `should return form flow state`() {

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/form-flow/{instanceId}",
                        formFlowInstanceId.id.toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.id").value(formFlowInstanceId.id.toString()))
            .andExpect(jsonPath("$.step").isNotEmpty)
            .andExpect(jsonPath("$.step.id").value(stepInstanceId.id.toString()))
            .andExpect(jsonPath("$.step.type").value("form"))
            .andExpect(jsonPath("$.step.typeProperties").isNotEmpty)
            .andExpect(jsonPath("$.step.typeProperties.definition.display").value("form"))
            .andExpect(jsonPath("$.step.typeProperties.definition.components").isNotEmpty)

        verify(stepInstance).open()
    }

    @Test
    fun `should fail gracefully when sending incorrect instance id`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/form-flow/{instanceId}",
                        UUID.randomUUID().toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(status().is4xxClientError)
            .andExpect(jsonPath("$.errorMessage").value("No form flow instance can be found for the given instance id"))
    }

    @Test
    fun `should complete step`() {
        whenever(formFlowInstance.complete(any(), any())).thenReturn(stepInstance)

        mockMvc.perform(post("/api/v1/form-flow/{flowId}/step/{stepId}", formFlowInstance.id.id, formFlowInstance.getCurrentStep().id.id))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(formFlowInstance.id.id.toString()))
            .andExpect(jsonPath("$.step.id").value(stepInstanceId.id.toString()))
            .andExpect(jsonPath("$.step.type").value("form"))
            .andExpect(jsonPath("$.step.typeProperties.definition.display").value("form"))
            .andExpect(jsonPath("$.step.typeProperties.definition.components").isNotEmpty)

        verify(stepInstance).open()
        verify(formFlowInstance).complete(any(), any())
    }

    @Test
    fun `should navigate to previous step`() {
        whenever(formFlowInstance.back()).thenReturn(stepInstance)

        mockMvc.perform(post("/api/v1/form-flow/{flowId}/back", formFlowInstance.id.id))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(formFlowInstance.id.id.toString()))
            .andExpect(jsonPath("$.step.id").value(stepInstanceId.id.toString()))
            .andExpect(jsonPath("$.step.type").value("form"))
            .andExpect(jsonPath("$.step.typeProperties.definition.display").value("form"))
            .andExpect(jsonPath("$.step.typeProperties.definition.components").isNotEmpty)

        verify(formFlowInstance).back()
        verify(stepInstance).open()
    }

    @Test
    fun `should save submission data when navigating to previous step`() {
        whenever(formFlowInstance.back()).thenReturn(stepInstance)

        mockMvc.perform(
            post("/api/v1/form-flow/{flowId}/back", formFlowInstance.id.id)
                .content("{\"step1\":\"A\"}")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.step.id").value(stepInstanceId.id.toString()))

        verify(formFlowInstance).save(any())
    }

    @Test
    fun `should save submission data`() {
        mockMvc.perform(
            post("/api/v1/form-flow/{flowId}/save", formFlowInstance.id.id)
                .content("{\"step1\":\"A\"}")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(formFlowInstance).save(any())
    }
}
