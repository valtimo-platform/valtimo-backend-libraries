package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.commandhandling.ExampleCommand
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.event.TestSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

class FormViewModelSubmissionServiceTest : BaseTest() {

    private lateinit var formViewModelSubmissionService: FormViewModelSubmissionService
    private lateinit var formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory
    private lateinit var camundaTaskService: CamundaTaskService
    private lateinit var camundaProcessService: CamundaProcessService
    private lateinit var testSubmissionHandler: TestSubmissionHandler
    private lateinit var objectMapper: ObjectMapper
    private lateinit var camundaTask: CamundaTask

    @BeforeEach
    fun setUp() {
        super.baseSetup()
        camundaTask = mock()
        camundaTaskService = mock()
        camundaProcessService = mock()
        testSubmissionHandler = TestSubmissionHandler()
        objectMapper = ObjectMapper()
        formViewModelSubmissionHandlerFactory = FormViewModelSubmissionHandlerFactory(
            formViewModelSubmissionHandlers = listOf(testSubmissionHandler)
        )
        formViewModelSubmissionService = FormViewModelSubmissionService(
            formViewModelSubmissionHandlerFactory = formViewModelSubmissionHandlerFactory,
            camundaTaskService = camundaTaskService,
            camundaProcessService = camundaProcessService,
            objectMapper = objectMapper
        )

        val processInstance = mock<CamundaExecution>()
        whenever(camundaTask.processInstance).thenReturn(processInstance)
        whenever(processInstance.businessKey).thenReturn("test")
    }

    @Test
    fun `should handle user task submission`() {
        val submission = submissionWithAdultAge()
        formViewModelSubmissionService.handleUserTaskSubmission(
            formName = "test",
            submission = submission,
            task = camundaTask
        )
        verify(commandDispatcher).dispatch(any<ExampleCommand>())
        verify(camundaTaskService).complete(camundaTask.id)
    }

    @Test
    fun `should not handle user task submission when exception thrown`() {
        val submission = submissionWithUnderAge()
        assertThrows<FormException> {
            formViewModelSubmissionService.handleUserTaskSubmission(
                formName = "test",
                submission = submission,
                task = camundaTask
            )
        }
        verify(camundaTaskService, never()).complete(any())
    }

    @Test
    fun `should handle start form submission`() {
        val submission = submissionWithAdultAge()
        formViewModelSubmissionService.handleStartFormSubmission(
            formName = "test",
            processDefinitionKey = "test",
            submission = submission
        )
        verify(commandDispatcher).dispatch(any<ExampleCommand>())
        verify(camundaProcessService).startProcess(eq("test"), any(), any())
    }

    @Test
    fun `should not handle start form submission when exception thrown`() {
        val submission = submissionWithUnderAge()
        assertThrows<FormException> {
            formViewModelSubmissionService.handleStartFormSubmission(
                formName = "test",
                processDefinitionKey = "test",
                submission = submission
            )
        }
        verify(camundaProcessService, never()).startProcess(any(), any(), any())
    }

    private fun submissionWithAdultAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "19")

    private fun submissionWithUnderAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "17")
}