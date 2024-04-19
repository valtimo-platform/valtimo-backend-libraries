package com.ritense.formviewmodel.web.rest

import com.ritense.formviewmodel.BaseTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FormViewModelResourceTest : BaseTest() {

    lateinit var mockMvc: MockMvc
    lateinit var resource: FormViewModelResource

    @BeforeEach
    fun setUp() {
        resource = FormViewModelResource()
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build()
    }

    @Test
    fun `should get form view model`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/form/view-model?formId=formId&taskInstanceId=taskInstanceId"
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should not get form view model`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/form/view-model"
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @Test
    fun `should update form view model`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post(
                        "/api/v1/form/view-model?formId=formId&taskInstanceId=taskInstanceId"
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("formViewModel")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should not update form view model`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post(
                        "/api/v1/form/view-model"
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }
}