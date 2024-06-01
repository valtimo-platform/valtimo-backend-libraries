package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.service.CamundaTaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FormViewModelServiceTest : BaseTest() {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var viewModelLoaderFactory: ViewModelLoaderFactory
    private lateinit var camundaTaskService: CamundaTaskService
    private lateinit var authorizationService: AuthorizationService
    private lateinit var formViewModelSubmissionService: FormViewModelSubmissionService
    private lateinit var camundaTask: CamundaTask

    private lateinit var formViewModelService: FormViewModelService

    @BeforeEach
    fun setUp() {
        objectMapper = MapperSingleton.get()
        viewModelLoaderFactory = mock()
        camundaTaskService = mock()
        authorizationService = mock()
        formViewModelSubmissionService = mock()

        formViewModelService = FormViewModelService(
            objectMapper = objectMapper,
            viewModelLoaderFactory = viewModelLoaderFactory,
            camundaTaskService = camundaTaskService,
            authorizationService = authorizationService,
            formViewModelSubmissionService = formViewModelSubmissionService
        )

        camundaTask = mock()
        whenever(camundaTaskService.findTaskById(any())).thenReturn(camundaTask)
    }

    @Test
    fun `should get ViewModel`() {
        whenever(viewModelLoaderFactory.getViewModelLoader("test")).thenReturn(TestViewModelLoader())

        val formViewModel = formViewModelService.getFormViewModel("test", "taskInstanceId")

        assertThat(formViewModel).isNotNull()
        assertThat(formViewModel!!.javaClass).isEqualTo(TestViewModel::class.java)
    }

    @Test
    fun `should return null for unknown ViewModel`() {
        val formViewModel = formViewModelService.getFormViewModel("test", "taskInstanceId")

        assertThat(formViewModel).isNull()
    }

    @Test
    fun `should update ViewModel`() {
        val viewModelLoader: TestViewModelLoader = mock()
        whenever(viewModelLoader.getViewModelType()).thenReturn(TestViewModel::class)

        whenever(viewModelLoaderFactory.getViewModelLoader("formName")).thenReturn(TestViewModelLoader())

        val updatedViewModel = formViewModelService.updateViewModel(
            formName = "formName",
            taskInstanceId = "taskInstanceId",
            submission = objectMapper.valueToTree(TestViewModel())
        )

        assertThat(updatedViewModel).isNotNull()
    }

    @Test
    fun `should submit ViewModel`() {
        whenever(viewModelLoaderFactory.getViewModelLoader("test")).thenReturn(TestViewModelLoader())

        formViewModelService.submit(
            formName = "test",
            taskInstanceId = "taskInstanceId",
            submission = objectMapper.valueToTree(TestViewModel())
        )
    }

    @Test
    fun `should return validation error for submission`() {
        whenever(formViewModelSubmissionService.handleSubmission(any(), any(), any())).then {
            throw FormException(message = "Im a child", "age")
        }

        assertThrows<FormException> {
            formViewModelService.submit(
                formName = "test",
                taskInstanceId = "taskInstanceId",
                submission = objectMapper.valueToTree(TestViewModel())
            )
        }
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

    fun submission(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("test", "test")
        .put("test2", "test2")

    data class RandomViewModel(
        val custom: String
    ) : ViewModel {

        override fun update(task: CamundaTask?): ViewModel {
            return this
        }
    }

}