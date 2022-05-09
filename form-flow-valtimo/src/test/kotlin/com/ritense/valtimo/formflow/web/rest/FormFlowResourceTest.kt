package com.ritense.valtimo.formflow.web.rest

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FormFlowResourceTest {
    lateinit var mockMvc: MockMvc
    lateinit var formFlowResource: FormFlowResource
    lateinit var formFlowService: FormFlowService
    lateinit var formFlowInstance: FormFlowInstance
    lateinit var formFlowInstanceId: FormFlowInstanceId

    @BeforeEach
    fun setUp() {
        formFlowService = mock()

        formFlowInstanceId = FormFlowInstanceId.newId()
        formFlowInstance = mock()
        whenever(formFlowInstance.id).thenReturn(formFlowInstanceId)
        whenever(formFlowService.getInstanceById(formFlowInstance.id)).thenReturn(formFlowInstance)
        formFlowResource = FormFlowResource(formFlowService)

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
            .andDo(print())
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.id").value(formFlowInstanceId.id.toString()))
            .andExpect(jsonPath("$.step").isNotEmpty)
            .andExpect(jsonPath("$.step.id").value(step1InstanceId.id.toString()))
            .andExpect(jsonPath("$.step.type").value("form"))
            .andExpect(jsonPath("$.step.type-properties").isNotEmpty)
            .andExpect(jsonPath("$.step.type-properties.definition").value("first-form-definition"))
    }
}