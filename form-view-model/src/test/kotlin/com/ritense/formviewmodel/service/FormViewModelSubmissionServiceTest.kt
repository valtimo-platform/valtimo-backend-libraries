package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.commandhandling.ExampleCommand
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.event.TestSubmissionHandler
import com.ritense.formviewmodel.json.MapperSingleton
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.never

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

    private fun submissionWithAdultAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "19")

    private fun submissionWithUnderAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "17")
}