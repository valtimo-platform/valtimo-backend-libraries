package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.json.MapperSingleton
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.formviewmodel.web.rest.error.FormViewModelModuleExceptionTranslator
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
    private lateinit var formViewModelModuleExceptionTranslator: FormViewModelModuleExceptionTranslator
    private var objectMapper = MapperSingleton.get()

    @BeforeEach
    fun setUp() {

        camundaTask = mock()
        viewModelLoaderFactory = mock()
        camundaTaskService = mock()
        authorizationService = mock()
        formViewModelSubmissionService = mock()
        formViewModelService = FormViewModelService(
            objectMapper = objectMapper
        )
        formViewModelModuleExceptionTranslator = FormViewModelModuleExceptionTranslator()
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
            .setControllerAdvice(formViewModelModuleExceptionTranslator)
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()
    }

    @Test
    fun `should get form view model`() {
        mockMvc.perform(
            get("/api/v1/form/view-model?formName=test&taskInstanceId=taskInstanceId")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
    }

    @Test
    fun `should not get form view model`() {
        mockMvc.perform(
            get("/api/v1/form/view-model")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should update form view model`() {
        whenever(viewModelLoaderFactory.getViewModelLoader(any())).thenReturn(TestViewModelLoader())

        mockMvc.perform(
            post(
                "/api/v1/form/view-model?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    fun `should not update form view model`() {
        mockMvc.perform(
            post("/api/v1/form/view-model")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should submit form view model`() {
        whenever(viewModelLoaderFactory.getViewModelLoader(any())).thenReturn(TestViewModelLoader())
        mockMvc.perform(
            post(
                "/api/v1/form/view-model/submit?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `should return validation error for submission`() {
        whenever(formViewModelSubmissionService.handleSubmission(any(), any(), any())).then {
            throw FormException(message = "Im a child", "age")
        }
        mockMvc.perform(
            post(
                "/api/v1/form/view-model/submit?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isBadRequest)
    }

}