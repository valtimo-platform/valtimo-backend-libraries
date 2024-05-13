package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.json.MapperSingleton
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

class FormViewModelResourceTest : BaseTest() {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: FormViewModelResource
    private lateinit var viewModelLoaderFactory: ViewModelLoaderFactory
    private lateinit var camundaTaskService: CamundaTaskService
    private lateinit var authorizationService: AuthorizationService
    private lateinit var formViewModelService: FormViewModelService
    private lateinit var formViewModelSubmissionService: FormViewModelSubmissionService
    private lateinit var camundaTask: CamundaTask

    @BeforeEach
    fun setUp() {
        camundaTask = mock()
        viewModelLoaderFactory = mock()
        camundaTaskService = mock()
        authorizationService = mock()
        formViewModelService = FormViewModelService(
            objectMapper = MapperSingleton.get()
        )
        formViewModelSubmissionService = mock()

        whenever(camundaTaskService.findTaskById(any())).thenReturn(camundaTask)

        resource = FormViewModelResource(
            viewModelLoaderFactory = viewModelLoaderFactory,
            camundaTaskService = camundaTaskService,
            authorizationService = authorizationService,
            formViewModelService = formViewModelService,
            formViewModelSubmissionService = formViewModelSubmissionService
        )
        mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()
    }

    @Test
    fun `should get form view model`() {
        mockMvc.perform(
            get(
                "/api/v1/form/view-model?formName=test&taskInstanceId=taskInstanceId"
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
        whenever(viewModelLoaderFactory.getViewModelLoader(any())).thenReturn(TestViewModelLoader())

        mockMvc.perform(
            post(
                "/api/v1/form/view-model?formName=test&taskInstanceId=taskInstanceId"
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
        whenever(viewModelLoaderFactory.getViewModelLoader(any())).thenReturn(TestViewModelLoader())
        mockMvc.perform(
            post(
                "/api/v1/form/view-model/submit?formName=test&taskInstanceId=taskInstanceId"
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}