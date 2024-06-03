package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.formviewmodel.BaseIntegrationTest
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

@Transactional
class FormViewModelResourceIntTest : BaseIntegrationTest() {

    @MockBean
    lateinit var camundaTaskService: CamundaTaskService

    @Autowired
    lateinit var formViewModelResource: FormViewModelResource

    lateinit var mockMvc: MockMvc

    private var objectMapper = MapperSingleton.get()

    @BeforeEach
    internal fun init() {
        mockMvc = MockMvcBuilders.standaloneSetup(formViewModelResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()

        val task: CamundaTask = mock()
        whenever(task.id).thenReturn("taskInstanceId")
        whenever(camundaTaskService.findTaskById(any())).thenReturn(task)
    }

    @Test
    fun `should get FormViewModel`() {
        runWithoutAuthorization {
            mockMvc.perform(
                get("${BASE_URL}?formName=test&taskInstanceId=taskInstanceId")
                    .accept(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(status().isOk)
        }
    }

    @Test
    fun `should update FormViewModel`() {
        runWithoutAuthorization {
            mockMvc.perform(
                post("${BASE_URL}?formName=test&taskInstanceId=taskInstanceId")
                    .accept(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
                    .contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(status().isOk)
        }
    }

    @Test
    fun `should submit FormViewModel`() {
        runWithoutAuthorization {
            mockMvc.perform(
                post("${BASE_URL}/submit?formName=test&taskInstanceId=taskInstanceId")
                    .accept(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(
                        objectMapper.writeValueAsString(
                            TestViewModel(
                                age = 22
                            )
                        )
                    )
            ).andExpect(status().isNoContent)
        }
    }

    companion object {
        private const val BASE_URL = "/api/v1/form/view-model"
    }
}