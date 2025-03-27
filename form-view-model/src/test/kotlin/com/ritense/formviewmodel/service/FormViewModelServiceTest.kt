package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.domain.FormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.FormDefinitionService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.viewmodel.TestFormViewModelLoader
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.service.CamundaTaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import java.util.Optional
import java.util.UUID


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormViewModelServiceTest(
    @Mock private val viewModelLoaderFactory: ViewModelLoaderFactory,
    @Mock private val camundaTaskService: CamundaTaskService,
    @Mock private val authorizationService: AuthorizationService,
    @Mock private val camundaTask: CamundaTask,
    @Mock private val processAuthorizationService: ProcessAuthorizationService,
    @Mock private val processLinkService: ProcessLinkService,
    @Mock private val formDefinitionService: FormDefinitionService,
    @Mock private val userTaskProcessLink: FormProcessLink,
    @Mock private val documentService: JsonSchemaDocumentService,
) : BaseTest() {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var formViewModelService: FormViewModelService


    @BeforeEach
    fun setUp() {
        objectMapper = MapperSingleton.get()

        formViewModelService = FormViewModelService(
            objectMapper = objectMapper,
            viewModelLoaderFactory = viewModelLoaderFactory,
            camundaTaskService = camundaTaskService,
            authorizationService = authorizationService,
            processAuthorizationService = processAuthorizationService,
            processLinkService = processLinkService,
            documentService = documentService,

        )

        val formDefinitionId = UUID.randomUUID()
        val processDefinition = mock<CamundaProcessDefinition>().apply {
            whenever(this.key).thenReturn(PROCESS_DEF_KEY)
        }
        whenever(camundaTask.processDefinition).thenReturn(processDefinition)
        whenever(camundaTaskService.findTaskById(any())).thenReturn(camundaTask)
        whenever(authorizationService.hasPermission<Boolean>(any())).thenReturn(true)
        whenever(userTaskProcessLink.formDefinitionId).thenReturn(formDefinitionId)
        whenever(userTaskProcessLink.activityType).thenReturn(ActivityTypeWithEventName.USER_TASK_CREATE)
        whenever(processLinkService.getProcessLinksByProcessDefinitionKey(
            eq(PROCESS_DEF_KEY),
        )).thenReturn(listOf(userTaskProcessLink))
        val definition = mock<FormDefinition>()
        whenever(definition.name).thenReturn("test")
        whenever(formDefinitionService.getFormDefinitionById(formDefinitionId)).thenReturn(Optional.of(definition))
    }

    @Test
    fun `should get ViewModel`() {
        whenever(viewModelLoaderFactory.getViewModelLoader(userTaskProcessLink)).thenReturn(TestFormViewModelLoader())

        val formViewModel = formViewModelService.getUserTaskFormViewModel("taskInstanceId")

        assertThat(formViewModel).isNotNull()
        assertThat(formViewModel!!.javaClass).isEqualTo(TestViewModel::class.java)
    }

    @Test
    fun `should return null for unknown ViewModel`() {
        val formViewModel = formViewModelService.getStartFormViewModel(
            processDefinitionKey = PROCESS_DEF_KEY
        )
        assertThat(formViewModel).isNull()
    }

    @Test
    fun `should update ViewModel`() {
        val viewModelLoader: TestFormViewModelLoader = mock()
        whenever(viewModelLoader.getViewModelType()).thenReturn(TestViewModel::class)

        whenever(viewModelLoaderFactory.getViewModelLoader(userTaskProcessLink)).thenReturn(TestFormViewModelLoader())

        val updatedViewModel = formViewModelService.updateUserTaskFormViewModel(
            taskInstanceId = "taskInstanceId",
            submission = objectMapper.valueToTree(TestViewModel()),
            page = 1
        )

        assertThat(updatedViewModel).isNotNull()
    }

    @Test
    fun `should parse ViewModel`() {
        val submission = submission()
        val viewModelInstance = formViewModelService.parseViewModel(
            submission = submission,
            viewModelType = TestViewModel::class
        )
        assertThat(viewModelInstance).isInstanceOf(TestViewModel::class.java)
        val viewModelInstanceCasted = viewModelInstance as TestViewModel
        assertThat(viewModelInstanceCasted.test).isEqualTo("test")
    }

    @Test
    fun `should not parse ViewModel of wrong type`() {
        val submission = submission()
        assertThrows<IllegalArgumentException> {
            formViewModelService.parseViewModel(
                submission = submission,
                viewModelType = RandomViewModel::class
            )
        }
    }

    private fun submission(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("test", "test")
        .put("test2", "test2")

    data class RandomViewModel(
        val custom: String
    ) : ViewModel {
        override fun update(task: CamundaTask?, page: Int?): ViewModel {
            return this
        }
    }

    companion object {
        const val PROCESS_DEF_KEY = "processDefinitionKey"
    }

}