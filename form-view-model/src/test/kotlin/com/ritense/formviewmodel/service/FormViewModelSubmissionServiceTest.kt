package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.TestStartFormSubmissionHandler
import com.ritense.formviewmodel.submission.TestUserTaskSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.service.CamundaTaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

class FormViewModelSubmissionServiceTest : BaseTest() {

    private lateinit var formViewModelSubmissionService: FormViewModelSubmissionService
    private lateinit var formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory
    private lateinit var formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory
    private lateinit var authorizationService: AuthorizationService
    private lateinit var camundaTaskService: CamundaTaskService
    private lateinit var testStartFormSubmissionHandler: TestStartFormSubmissionHandler
    private lateinit var testUserTaskSubmissionHandler: TestUserTaskSubmissionHandler
    private lateinit var objectMapper: ObjectMapper
    private lateinit var camundaTask: CamundaTask
    private lateinit var processAuthorizationService: ProcessAuthorizationService
    private val businessKey = "businessKey"

    @BeforeEach
    fun setUp() {
        authorizationService = mock()
        camundaTask = mock()
        camundaTaskService = mock()
        testUserTaskSubmissionHandler = spy()
        testStartFormSubmissionHandler = spy()
        objectMapper = ObjectMapper()
        processAuthorizationService = mock()
        formViewModelStartFormSubmissionHandlerFactory = FormViewModelStartFormSubmissionHandlerFactory(
            handlers = listOf(testStartFormSubmissionHandler)
        )
        formViewModelUserTaskSubmissionHandlerFactory = FormViewModelUserTaskSubmissionHandlerFactory(
            handlers = listOf(testUserTaskSubmissionHandler)
        )
        formViewModelSubmissionService = FormViewModelSubmissionService(
            formViewModelStartFormSubmissionHandlerFactory = formViewModelStartFormSubmissionHandlerFactory,
            formViewModelUserTaskSubmissionHandlerFactory = formViewModelUserTaskSubmissionHandlerFactory,
            authorizationService = authorizationService,
            camundaTaskService = camundaTaskService,
            objectMapper = objectMapper,
            processAuthorizationService = processAuthorizationService
        )

        val processInstance = mock<CamundaExecution>()
        whenever(camundaTask.processInstance).thenReturn(processInstance)
        whenever(processInstance.businessKey).thenReturn(businessKey)
        whenever(camundaTaskService.findTaskById(any())).thenReturn(camundaTask)
    }

    @Test
    fun `should handle user task submission`() {
        val submission = submissionWithAdultAge()
        formViewModelSubmissionService.handleUserTaskSubmission(
            formName = "test",
            submission = submission,
            taskInstanceId = "taskInstanceId"
        )
        val submissionCaptor = argumentCaptor<TestViewModel>()
        val taskCaptor = argumentCaptor<CamundaTask>()
        val businessKeyCaptor = argumentCaptor<String>()
        verify(testUserTaskSubmissionHandler).handle(
            submission = submissionCaptor.capture(),
            task = taskCaptor.capture(),
            businessKey = businessKeyCaptor.capture()
        )
        assertThat(submissionCaptor.firstValue).isInstanceOf(TestViewModel::class.java)
        assertThat(taskCaptor.firstValue).isEqualTo(camundaTask)
        assertThat(businessKeyCaptor.firstValue).isEqualTo(businessKey)
    }

    @Test
    fun `should not handle user task submission when exception thrown`() {
        val submission = submissionWithUnderAge()
        assertThrows<FormException> {
            formViewModelSubmissionService.handleUserTaskSubmission(
                formName = "test",
                submission = submission,
                taskInstanceId = "taskInstanceId"
            )
        }
    }

    @Test
    fun `should handle start form submission`() {
        val submission = submissionWithAdultAge()
        val documentDefinitionName = "documentDefinitionName"
        val processDefinitionKey = "processDefinitionKey"
        formViewModelSubmissionService.handleStartFormSubmission(
            formName = "test",
            processDefinitionKey = processDefinitionKey,
            documentDefinitionName = documentDefinitionName,
            submission = submission
        )
        val documentDefinitionNameCaptor = argumentCaptor<String>()
        val processDefinitionKeyCaptor = argumentCaptor<String>()
        val submissionCaptor = argumentCaptor<TestViewModel>()

        verify(testStartFormSubmissionHandler).handle(
            documentDefinitionName = documentDefinitionNameCaptor.capture(),
            processDefinitionKey = processDefinitionKeyCaptor.capture(),
            submission = submissionCaptor.capture()
        )
        assertThat(documentDefinitionNameCaptor.firstValue).isEqualTo(documentDefinitionName)
        assertThat(processDefinitionKeyCaptor.firstValue).isEqualTo(processDefinitionKey)
        assertThat(submissionCaptor.firstValue).isInstanceOf(TestViewModel::class.java)
    }

    @Test
    fun `should not handle start form submission when exception thrown`() {
        val submission = submissionWithUnderAge()
        assertThrows<FormException> {
            formViewModelSubmissionService.handleStartFormSubmission(
                formName = "test",
                processDefinitionKey = "processDefinitionKey",
                documentDefinitionName = "documentDefinitionName",
                submission = submission
            )
        }
    }

    private fun submissionWithAdultAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "19")

    private fun submissionWithUnderAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "17")
}