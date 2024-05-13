package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.commandhandling.ExampleCommand
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.event.TestEventHandler
import com.ritense.formviewmodel.json.MapperSingleton
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any

class FormViewModelSubmissionServiceTest : BaseTest() {

    private lateinit var formViewModelSubmissionService: FormViewModelSubmissionService
    private lateinit var formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory
    private lateinit var camundaTaskService: CamundaTaskService
    private lateinit var testEventHandler: TestEventHandler

    @BeforeEach
    fun setUp() {
        super.baseSetup()
        camundaTaskService = mock()
        testEventHandler = TestEventHandler()
        formViewModelSubmissionHandlerFactory = FormViewModelSubmissionHandlerFactory(
            formViewModelSubmissionHandlers = listOf(testEventHandler)
        )
        formViewModelSubmissionService = FormViewModelSubmissionService(
            formViewModelSubmissionHandlerFactory = formViewModelSubmissionHandlerFactory,
            camundaTaskService = camundaTaskService
        )
    }

    @Test
    fun `should handle submission`() {
        val submission = submissionWithAdultAge()
        formViewModelSubmissionService.handleSubmission(
            formName = "test",
            submission = submission,
            taskInstanceId = "test"
        )
        verify(commandDispatcher).dispatch(any<ExampleCommand>())
        verify(camundaTaskService).complete("test")
    }

    @Test
    fun `should not handle submission`() {
        val submission = submissionWithUnderAge()

        assertThrows<IllegalArgumentException> {
            formViewModelSubmissionService.handleSubmission(
                formName = "test",
                submission = submission,
                taskInstanceId = "test"
            )
        }
    }

    fun submissionWithAdultAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "19")

    fun submissionWithUnderAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "17")
}