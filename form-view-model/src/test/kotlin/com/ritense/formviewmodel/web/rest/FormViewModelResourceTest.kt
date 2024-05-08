package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.formviewmodel.json.MapperSingleton
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FormViewModelResourceTest : BaseTest() {

    lateinit var mockMvc: MockMvc
    lateinit var resource: FormViewModelResource
    lateinit var viewModelLoaderFactory: ViewModelLoaderFactory
    lateinit var camundaTaskService: CamundaTaskService
    lateinit var authorizationService: AuthorizationService
    lateinit var formViewModelService: FormViewModelService
    lateinit var formViewModelSubmissionService: FormViewModelSubmissionService

    @BeforeEach
    fun setUp() {
        viewModelLoaderFactory = mock()
        camundaTaskService = mock()
        authorizationService = mock()
        formViewModelService = FormViewModelService(
            objectMapper = MapperSingleton.get()
        )
        formViewModelSubmissionService = mock()

        resource = FormViewModelResource(
            viewModelLoaderFactory = viewModelLoaderFactory,
            camundaTaskService = camundaTaskService,
            authorizationService = authorizationService,
            formViewModelService = formViewModelService,
            formViewModelSubmissionService = formViewModelSubmissionService
        )
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build()
    }

    @Test
    fun `should get form view model`() {
        mockMvc.perform(
            get(
                "/api/v1/form/view-model?formName=formName&taskInstanceId=taskInstanceId"
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should not get form view model`() {
        mockMvc.perform(
            get(
                "/api/v1/form/view-model"
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @Test
    fun `should update form view model`() {
        whenever(viewModelLoaderFactory.getViewModelLoader("formName")).thenReturn(TestViewModelLoader())

        mockMvc.perform(
            post(
                "/api/v1/form/view-model?formName=formName&taskInstanceId=taskInstanceId"
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should not update form view model`() {
        mockMvc.perform(
            post(
                "/api/v1/form/view-model"
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @Test
    fun `should submit form view model`() {
        whenever(viewModelLoaderFactory.getViewModelLoader("formName")).thenReturn(TestViewModelLoader())
        mockMvc.perform(
            post(
                "/api/v1/form/view-model/submit?formName=formName&taskInstanceId=taskInstanceId"
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}