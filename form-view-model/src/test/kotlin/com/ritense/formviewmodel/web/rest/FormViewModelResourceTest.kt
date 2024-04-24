package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.domain.factory.ViewModelLoaderFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FormViewModelResourceTest : BaseTest() {

    lateinit var mockMvc: MockMvc
    lateinit var resource: FormViewModelResource
    lateinit var viewModelLoaderFactory: ViewModelLoaderFactory

    @BeforeEach
    fun setUp() {
        viewModelLoaderFactory = mock()
        resource = FormViewModelResource(viewModelLoaderFactory)
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
        whenever(viewModelLoaderFactory.getViewModelLoader("formId")).thenReturn(TestViewModelLoader())

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post(
                        "/api/v1/form/view-model?formId=formId&taskInstanceId=taskInstanceId"
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
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