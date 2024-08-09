package com.ritense.form.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.form.BaseIntegrationTest
import com.ritense.form.service.IntermediateSubmissionService
import com.ritense.form.web.rest.dto.IntermediateSaveRequest
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
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
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

class IntermediateSubmissionResourceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var intermediateSubmissionResource: IntermediateSubmissionResource

    @Autowired
    lateinit var intermediateSubmissionService: IntermediateSubmissionService

    @MockBean
    lateinit var camundaTaskService: CamundaTaskService

    lateinit var mockMvc: MockMvc

    private var objectMapper = MapperSingleton.get()

    @BeforeEach
    internal fun init() {
        mockMvc = MockMvcBuilders.standaloneSetup(intermediateSubmissionResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()
        val task: CamundaTask = mock()
        val execution: CamundaExecution = mock()
        whenever(task.processInstance).thenReturn(execution)
        whenever(execution.businessKey).thenReturn("a business Key")
        whenever(task.id).thenReturn("taskInstanceId")
        whenever(camundaTaskService.findTaskById(any())).thenReturn(task)

        val manageableUser: ManageableUser = mock()
        whenever(manageableUser.userIdentifier).thenReturn("userIdentifier")
        whenever(manageableUser.fullName).thenReturn("FullName")
        whenever(userManagementService.currentUser).thenReturn(manageableUser)
        whenever(userManagementService.findByUserIdentifier(any())).thenReturn(manageableUser)
    }

    @Test
    fun `should return 404 for intermediate submission not found`() {
        runWithoutAuthorization {
            mockMvc.perform(
                get("$BASE_URL?taskInstanceId=taskInstanceId")
                    .accept(APPLICATION_JSON_UTF8_VALUE)
                    .contentType(APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(status().isNotFound)
        }
    }

    @Test
    @WithMockUser
    fun `should return 200 for intermediate submission found`() {
        runWithoutAuthorization {
            intermediateSubmissionService.store(
                submission = objectMapper.createObjectNode(),
                taskInstanceId = "taskInstanceId"
            )
            mockMvc.perform(
                get("$BASE_URL?taskInstanceId=taskInstanceId")
                    .accept(APPLICATION_JSON_UTF8_VALUE)
                    .contentType(APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(status().isOk)
        }
    }

    @Test
    @WithMockUser
    fun `should return 200 for store intermediate submission`() {
        runWithoutAuthorization {
            val submission = objectMapper.createObjectNode().apply { put("key", "value") }
            val request = IntermediateSaveRequest(
                submission = submission,
                taskInstanceId = "taskInstanceId"
            )
            val requestJson = jacksonObjectMapper().writeValueAsString(request)
            mockMvc.perform(
                post(BASE_URL)
                    .accept(APPLICATION_JSON_UTF8_VALUE)
                    .content(requestJson)
                    .contentType(APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.submission").isNotEmpty)
                .andExpect(jsonPath("$.taskInstanceId").value(request.taskInstanceId))
                .andExpect(jsonPath("$.createdBy").value("FullName"))
                .andExpect(jsonPath("$.createdOn").isNotEmpty)
                .andExpect(jsonPath("$.editedBy").isEmpty)
                .andExpect(jsonPath("$.editedOn").isEmpty)
        }
    }

    @Test
    @WithMockUser
    fun `should return 200 deleting intermediate submission`() {
        runWithoutAuthorization {
            val intermediateSubmission = intermediateSubmissionService.store(
                submission = objectMapper.createObjectNode(),
                taskInstanceId = "taskInstanceId"
            )
            mockMvc.perform(
                delete(
                    "$BASE_URL?taskInstanceId={taskInstanceId}",
                    intermediateSubmission.taskInstanceId
                ).accept(APPLICATION_JSON_UTF8_VALUE)
                    .contentType(APPLICATION_JSON_UTF8_VALUE)
            ).andExpect(status().isOk)
        }
    }

    companion object {
        const val BASE_URL = "/api/v1/form/intermediate/submission"
    }
}