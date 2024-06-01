package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.ValtimoAuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.formviewmodel.BaseIntegrationTest
import com.ritense.formviewmodel.validation.OnStartUpViewModelValidator
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

@Transactional
class FormViewModelResourceIntTest : BaseIntegrationTest() {

    @MockBean
    lateinit var camundaTaskService: CamundaTaskService

    @Autowired
    lateinit var formViewModelResource: FormViewModelResource

    lateinit var mockMvc: MockMvc

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
                MockMvcRequestBuilders.get("${BASE_URL}?formName=test&taskInstanceId=taskInstanceId")
                    .accept(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(MockMvcResultMatchers.status().isOk)
        }
    }

    @Test
    fun `should update FormViewModel`() {
        runWithoutAuthorization {
            mockMvc.perform(
                MockMvcRequestBuilders.post("${BASE_URL}?formName=test&taskInstanceId=taskInstanceId")
                    .accept(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(jacksonObjectMapper().writeValueAsString(TestViewModel()))
                    .contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(MockMvcResultMatchers.status().isOk)
        }
    }

    @Test
    fun `should submit FormViewModel`() {
        runWithoutAuthorization {
            mockMvc.perform(
                MockMvcRequestBuilders.post("${BASE_URL}/submit?formName=test&taskInstanceId=taskInstanceId")
                    .accept(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(jacksonObjectMapper().writeValueAsString(
                        TestViewModel(
                            age = 22
                        )
                    ))
            ).andExpect(MockMvcResultMatchers.status().isNoContent)
        }
    }

    companion object {
        private const val BASE_URL = "/api/v1/form/view-model"
    }
}