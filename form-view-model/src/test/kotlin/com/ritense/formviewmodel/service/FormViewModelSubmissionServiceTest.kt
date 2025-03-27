package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.domain.FormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.FormDefinitionService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.service.FormViewModelServiceTest.Companion.PROCESS_DEF_KEY
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.TestStartFormSubmissionHandler
import com.ritense.formviewmodel.submission.TestUserTaskSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.service.CamundaTaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormViewModelSubmissionServiceTest(
    @Mock private var authorizationService: AuthorizationService,
    @Mock private val camundaTaskService: CamundaTaskService,
    @Mock private val processAuthorizationService: ProcessAuthorizationService,
    @Mock private val formDefinitionService: FormDefinitionService,
    @Mock private val camundaTask: CamundaTask,
    @Mock private val processLinkService: ProcessLinkService,
    @Mock private val userTaskProcessLink: FormProcessLink,
    @Mock private val startEventProcessLink: FormProcessLink,
    @Mock private val documentService: JsonSchemaDocumentService,
) : BaseTest() {

    private val objectMapper = ObjectMapper()
    private lateinit var testStartFormSubmissionHandler: TestStartFormSubmissionHandler
    private lateinit var testUserTaskSubmissionHandler: TestUserTaskSubmissionHandler
    private lateinit var formViewModelSubmissionService: FormViewModelSubmissionService
    private lateinit var formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory
    private lateinit var formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory

    @BeforeEach
    fun setUp() {
        testStartFormSubmissionHandler = spy(TestStartFormSubmissionHandler(formDefinitionService = formDefinitionService))
        formViewModelStartFormSubmissionHandlerFactory = FormViewModelStartFormSubmissionHandlerFactory(
            handlers = listOf(testStartFormSubmissionHandler),
            formDefinitionService = formDefinitionService,
        )

        testUserTaskSubmissionHandler = spy(TestUserTaskSubmissionHandler())
        formViewModelUserTaskSubmissionHandlerFactory = FormViewModelUserTaskSubmissionHandlerFactory(
            handlers = listOf(testUserTaskSubmissionHandler),
            formDefinitionService = formDefinitionService,
        )
        formViewModelSubmissionService = FormViewModelSubmissionService(
            formViewModelStartFormSubmissionHandlerFactory = formViewModelStartFormSubmissionHandlerFactory,
            formViewModelUserTaskSubmissionHandlerFactory = formViewModelUserTaskSubmissionHandlerFactory,
            authorizationService = authorizationService,
            camundaTaskService = camundaTaskService,
            objectMapper = objectMapper,
            processAuthorizationService = processAuthorizationService,
            processLinkService = processLinkService,
            documentService = documentService,
        )

        val processInstance = mock<CamundaExecution>().apply {
            whenever(this.businessKey).thenReturn(BUSINESS_KEY)
        }
        val processDefinition = mock<CamundaProcessDefinition>().apply {
            whenever(this.key).thenReturn(PROC_DEF_KEY)
        }
        whenever(camundaTask.processInstance).thenReturn(processInstance)
        whenever(camundaTask.processDefinition).thenReturn(processDefinition)
        whenever(camundaTaskService.findTaskById(TASK_INSTANCE_ID)).thenReturn(camundaTask)

        val formDefinitionId = UUID.randomUUID()
        whenever(userTaskProcessLink.formDefinitionId).thenReturn(formDefinitionId)
        whenever(userTaskProcessLink.activityType).thenReturn(ActivityTypeWithEventName.USER_TASK_CREATE)
        whenever(startEventProcessLink.formDefinitionId).thenReturn(formDefinitionId)
        whenever(startEventProcessLink.activityType).thenReturn(ActivityTypeWithEventName.START_EVENT_START)
        whenever(processLinkService.getProcessLinksByProcessDefinitionKey(PROCESS_DEF_KEY)).thenReturn(listOf(startEventProcessLink, userTaskProcessLink))

        val definition = org.mockito.kotlin.mock<FormDefinition>()
        whenever(definition.name).thenReturn("test")
        whenever(formDefinitionService.getFormDefinitionById(formDefinitionId)).thenReturn(Optional.of(definition))
    }

    @Test
    fun `should handle user task submission`() {
        val submission = submissionWithAdultAge()
        formViewModelSubmissionService.handleUserTaskSubmission(
            submission = submission,
            taskInstanceId = TASK_INSTANCE_ID
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
        assertThat(businessKeyCaptor.firstValue).isEqualTo(BUSINESS_KEY)
    }

    @Test
    fun `should not handle user task submission when exception thrown`() {
        val submission = submissionWithUnderAge()
        assertThrows<FormException> {
            formViewModelSubmissionService.handleUserTaskSubmission(
                submission = submission,
                taskInstanceId = TASK_INSTANCE_ID
            )
        }
    }

    @Test
    fun `should handle start form submission`() {
        val submission = submissionWithAdultAge()
        formViewModelSubmissionService.handleStartFormSubmission(
            processDefinitionKey = PROC_DEF_KEY,
            documentDefinitionName = DOC_DEF_NAME,
            submission = submission,
        )
        val documentDefinitionNameCaptor = argumentCaptor<String>()
        val processDefinitionKeyCaptor = argumentCaptor<String>()
        val submissionCaptor = argumentCaptor<TestViewModel>()

        verify(testStartFormSubmissionHandler).handle(
            documentDefinitionName = documentDefinitionNameCaptor.capture(),
            processDefinitionKey = processDefinitionKeyCaptor.capture(),
            submission = submissionCaptor.capture(),
            document = isNull()
        )
        assertThat(documentDefinitionNameCaptor.firstValue).isEqualTo(DOC_DEF_NAME)
        assertThat(processDefinitionKeyCaptor.firstValue).isEqualTo(PROC_DEF_KEY)
        assertThat(submissionCaptor.firstValue).isInstanceOf(TestViewModel::class.java)
    }

    @Test
    fun `should not handle start form submission when exception thrown`() {
        val submission = submissionWithUnderAge()
        assertThrows<FormException> {
            formViewModelSubmissionService.handleStartFormSubmission(
                processDefinitionKey = PROC_DEF_KEY,
                documentDefinitionName = DOC_DEF_NAME,
                submission = submission
            )
        }
    }

    private fun submissionWithAdultAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "19")

    private fun submissionWithUnderAge(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("age", "17")

    companion object {
        const val BUSINESS_KEY = "businessKey"
        const val PROC_DEF_KEY = "processDefinitionKey"
        const val DOC_DEF_NAME = "documentDefinitionName"
        const val TASK_INSTANCE_ID = "taskInstanceId"
    }
}