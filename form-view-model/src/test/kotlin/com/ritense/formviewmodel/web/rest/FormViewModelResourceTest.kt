package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.web.rest.error.FormViewModelModuleExceptionTranslator
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FormViewModelResourceTest(
    @Mock private val formViewModelService: FormViewModelService,
    @Mock private val formViewModelSubmissionService: FormViewModelSubmissionService,
) : BaseTest() {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: FormViewModelResource
    private lateinit var formViewModelModuleExceptionTranslator: FormViewModelModuleExceptionTranslator
    private var objectMapper = MapperSingleton.get()

    @BeforeEach
    fun setUp() {
        formViewModelModuleExceptionTranslator = FormViewModelModuleExceptionTranslator()

        resource = FormViewModelResource(
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
    fun `should get user task view model`() {
        whenever(
            formViewModelService.getUserTaskFormViewModel(
                taskInstanceId = eq("taskInstanceId")
            )
        ).thenReturn(TestViewModel())
        mockMvc.perform(
            get("$BASE_URL/$USER_TASK?formName=test&taskInstanceId=taskInstanceId")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
    }

    @Test
    fun `should return notfound for unknown user task view model`() {
        mockMvc.perform(
            get("$BASE_URL/$USER_TASK?formName=test&taskInstanceId=taskInstanceId")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `should not get user task view model`() {
        mockMvc.perform(
            get("$BASE_URL/$USER_TASK")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should update user task view model`() {
        whenever(
            formViewModelService.updateUserTaskFormViewModel(
                taskInstanceId = anyOrNull(),
                submission = anyOrNull(),
                page = anyOrNull(),
            )
        ).thenReturn(TestViewModel())

        mockMvc.perform(
            post(
                "$BASE_URL/$USER_TASK?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    fun `should not update user task view model`() {
        mockMvc.perform(
            post("$BASE_URL/$USER_TASK")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should submit user task view model`() {
        mockMvc.perform(
            post(
                "$BASE_URL/submit/$USER_TASK?formName={formName}&taskInstanceId={taskInstanceId}",
                "test", "taskInstanceId"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `should return validation error for user task submission`() {
        val taskInstanceId = "taskInstanceId"
        whenever(
            formViewModelSubmissionService.handleUserTaskSubmission(
                submission = any(),
                taskInstanceId = eq(taskInstanceId)
            )
        ).then {
            throw FormException(message = "Im a child", "age")
        }
        mockMvc.perform(
            post(
                "$BASE_URL/submit/$USER_TASK"
            ).queryParam("taskInstanceId", taskInstanceId)
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Im a child"))
            .andExpect(jsonPath("$.component").value("age"))
    }

    // start form tests

    @Test
    fun `should get start form view model`() {
        whenever(
            formViewModelService.getStartFormViewModel(
                processDefinitionKey = eq("processDefinitionKey"),
                documentId = eq(null)
            )
        ).thenReturn(TestViewModel())
        mockMvc.perform(
            get("$BASE_URL/$START_FORM?formName=test&processDefinitionKey=processDefinitionKey")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
    }

    @Test
    fun `should get start form view model with documentId`() {
        whenever(
            formViewModelService.getStartFormViewModel(
                processDefinitionKey = eq("processDefinitionKey"),
                documentId = eq(UUID.fromString("c91f14fe-aebb-4598-8485-98d1c4ac1cc8"))
            )
        ).thenReturn(TestViewModel())
        mockMvc.perform(
            get("$BASE_URL/$START_FORM?formName=test&processDefinitionKey=processDefinitionKey&documentId=c91f14fe-aebb-4598-8485-98d1c4ac1cc8")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
    }

    @Test
    fun `should return notfound for unknown start form view model`() {
        mockMvc.perform(
            get("$BASE_URL/$START_FORM?formName=test&processDefinitionKey=processDefinitionKey")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `should not get start form view model`() {
        mockMvc.perform(
            get("$BASE_URL/$START_FORM")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should update start form view model`() {
        whenever(
            formViewModelService.updateStartFormViewModel(
                submission = anyOrNull(),
                processDefinitionKey = anyOrNull(),
                page = anyOrNull(),
                documentId = anyOrNull()
            )
        ).thenReturn(TestViewModel())
        mockMvc.perform(
            post(
                "$BASE_URL/$START_FORM?formName={formName}&processDefinitionKey={processDefinitionKey}",
                "test", "processDefinitionKey"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        ).andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    fun `should update start form view model with document ID`() {
        whenever(
            formViewModelService.updateStartFormViewModel(
                submission = anyOrNull(),
                processDefinitionKey = anyOrNull(),
                page = anyOrNull(),
                documentId = anyOrNull()
            )
        ).thenReturn(TestViewModel())
        mockMvc.perform(
            post(
                "$BASE_URL/$START_FORM?formName={formName}&processDefinitionKey={processDefinitionKey}&documentId={documentId}",
                "test", "processDefinitionKey", "c91f14fe-aebb-4598-8485-98d1c4ac1cc8"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
        ).andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    fun `should not update start form view model`() {
        mockMvc.perform(
            post("$BASE_URL/$START_FORM")
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().is4xxClientError)
    }

    @Test
    fun `should submit start form view model`() {
        mockMvc.perform(
            post(
                "$BASE_URL/submit/$START_FORM?" +
                    "formName={formName}&" +
                    "processDefinitionKey={processDefinitionKey}&" +
                    "documentDefinitionName={documentDefinitionName}",
                "test", "processDefinitionKey", "documentDefinitionName"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isOk)
    }

    @Test
    fun `should submit start form view model with documentId`() {
        mockMvc.perform(
            post(
                "$BASE_URL/submit/$START_FORM?" +
                    "formName={formName}&" +
                    "processDefinitionKey={processDefinitionKey}&" +
                    "documentDefinitionName={documentDefinitionName}" +
                    "documentId={documentId}",
                "test", "processDefinitionKey", "documentDefinitionName", "c91f14fe-aebb-4598-8485-98d1c4ac1cc8"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isOk)
    }

    @Test
    fun `should return validation error for start form submission`() {
        val processDefinitionKey = "processDefinitionKey"
        val documentDefinitionName = "documentDefinitionName"
        whenever(
            formViewModelSubmissionService.handleStartFormSubmission(
                processDefinitionKey = eq(processDefinitionKey),
                documentDefinitionName = eq(documentDefinitionName),
                submission = any(),
                documentId = anyOrNull()
            )
        ).then {
            throw FormException(message = "Im a child", "age")
        }
        mockMvc.perform(
            post("$BASE_URL/submit/$START_FORM")
                .queryParam("processDefinitionKey", processDefinitionKey)
                .queryParam("documentDefinitionName", documentDefinitionName)
                .accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(TestViewModel()))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Im a child"))
            .andExpect(jsonPath("$.component").value("age"))
    }

    companion object {
        const val BASE_URL = "/api/v1/form/view-model"
        const val START_FORM = "start-form"
        const val USER_TASK = "user-task"
    }

}