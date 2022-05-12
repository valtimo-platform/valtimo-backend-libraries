package com.ritense.valtimo.formflow.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.form.service.FormDefinitionService
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
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
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class FormFlowResourceTest : BaseTest() {
    lateinit var mockMvc: MockMvc
    lateinit var formFlowResource: FormFlowResource
    lateinit var formFlowService: FormFlowService
    lateinit var formDefinitionService: FormDefinitionService
    lateinit var formFlowInstance: FormFlowInstance
    lateinit var formFlowInstanceId: FormFlowInstanceId

    @BeforeEach
    fun setUp() {
        formFlowService = mock()
        formDefinitionService = mock()
        whenever(formFlowService.getTypeProperties(any(), any())).thenReturn(
            FormTypeProperties(
                jacksonObjectMapper().readTree(
                    readFileAsString("/config/form/user-task-lening-aanvragen.json")
                )
            )
        )

        formFlowInstanceId = FormFlowInstanceId.newId()
        formFlowInstance = mock()
        whenever(formFlowInstance.id).thenReturn(formFlowInstanceId)
        whenever(formFlowService.getByInstanceIdIfExists(formFlowInstance.id)).thenReturn(formFlowInstance)
        formFlowResource = FormFlowResource(formFlowService, formDefinitionService)

        mockMvc = MockMvcBuilders.standaloneSetup(formFlowResource).build()
    }

    @Test
    fun `should return form flow state`() {
        val step1 = FormFlowStep(
            FormFlowStepId("key2"),
            type = FormFlowStepType("form", FormStepTypeProperties("first-form-definition"))
        )
        val step2 = FormFlowStep(
            FormFlowStepId("key3"),
            type = FormFlowStepType("form", FormStepTypeProperties("second-form-definition"))
        )
        val definition = FormFlowDefinition(
            id = FormFlowDefinitionId.newId("key1"), "step1", mutableSetOf(step1, step2)
        )
        whenever(formFlowInstance.formFlowDefinition).thenReturn(definition)

        val step1InstanceId = FormFlowStepInstanceId.newId()
        val step1Instance = FormFlowStepInstance(
            step1InstanceId,
            formFlowInstance,
            step1.id.key,
            1,
            null
        )

        whenever(formFlowInstance.getCurrentStep()).thenReturn(step1Instance)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/form-flow/{instanceId}",
                        formFlowInstanceId.id.toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.id").value(formFlowInstanceId.id.toString()))
            .andExpect(jsonPath("$.step").isNotEmpty)
            .andExpect(jsonPath("$.step.id").value(step1InstanceId.id.toString()))
            .andExpect(jsonPath("$.step.type").value("form"))
            .andExpect(jsonPath("$.step.typeProperties.definition.display").value("form"))
            .andExpect(jsonPath("$.step.typeProperties.definition.components").isNotEmpty)
    }

    @Test
    fun `should fail gracefully when sending incorrect instance id`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/form-flow/{instanceId}",
                        UUID.randomUUID().toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(status().is4xxClientError)
            .andExpect(jsonPath("$.errorMessage").value("No form flow instance can be found for the given instance id"))
    }
}
