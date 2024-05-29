package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.web.rest.error.FormViewModelModuleExceptionTranslator
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
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
    private lateinit var formViewModelService: FormViewModelService
    private lateinit var formViewModelModuleExceptionTranslator: FormViewModelModuleExceptionTranslator
    private var objectMapper = MapperSingleton.get()

    @BeforeEach
    fun setUp() {
        formViewModelService = mock()
        formViewModelModuleExceptionTranslator = FormViewModelModuleExceptionTranslator()

        resource = FormViewModelResource(
            formViewModelService = formViewModelService
        )
        mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .setControllerAdvice(formViewModelModuleExceptionTranslator)
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()
    }

    @Test
    fun `should get view model`() {
        whenever(formViewModelService.getFormViewModel(
            "test",
            "taskInstanceId")
        ).thenReturn(TestViewModel())

        mockMvc.perform(
            get("$BASE_URL?formName=test&taskInstanceId=taskInstanceId")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
    }

    @Test
    fun `should return notfound for unknown view model`() {
        mockMvc.perform(
            get("$BASE_URL?formName=test&taskInstanceId=taskInstanceId")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isNotFound)
    }


    @Test
    fun `should not get view model`() {
        mockMvc.perform(
            get("$BASE_URL")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should update view model`() {
        whenever(formViewModelService.updateViewModel(any(), any(), any())).thenReturn(TestViewModel())

        mockMvc.perform(
            post(
                "$BASE_URL?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    fun `should not update view model`() {
        mockMvc.perform(
            post("$BASE_URL")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should submit view model`() {
        mockMvc.perform(
            post(
                "$BASE_URL/submit?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `should return validation error for submission`() {
        whenever(formViewModelService.submit(any(), any(), any())).thenAnswer {
            throw FormException(message = "Im a child", "age")
        }
        mockMvc.perform(
            post(
                "$BASE_URL/submit?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isBadRequest)
    }

    companion object {
        private const val BASE_URL = "/api/v1/form/view-model"
    }

}