package com.ritense.formviewmodel.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.TestStartFormSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Optional
import kotlin.reflect.KClass
import kotlin.test.assertTrue

class OnStartUpViewModelValidatorTest : BaseTest() {
    private lateinit var formIoFormDefinitionService: FormIoFormDefinitionService
    private lateinit var onStartUpViewModelValidator: OnStartUpViewModelValidator
    private lateinit var viewModelLoaders: List<ViewModelLoader<*>>
    private lateinit var viewModelLoader: ViewModelLoader<ViewModel>
    private lateinit var formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory
    private lateinit var formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory

    @BeforeEach
    fun setUp() {
        formIoFormDefinitionService = mock()
        viewModelLoader = mock(ViewModelLoader::class.java) as ViewModelLoader<ViewModel>
        viewModelLoaders = listOf(viewModelLoader)
        formViewModelStartFormSubmissionHandlerFactory = mock()
        formViewModelUserTaskSubmissionHandlerFactory = mock()
        onStartUpViewModelValidator = OnStartUpViewModelValidator(
            formIoFormDefinitionService,
            viewModelLoaders,
            formViewModelStartFormSubmissionHandlerFactory,
            formViewModelUserTaskSubmissionHandlerFactory
        )
    }

    @Test
    fun `should not find missing fields when all ViewModel fields match form`() {
        val testViewModelLoader = TestViewModelLoader()
        val missingFields = onStartUpViewModelValidator.validateViewModel(
            testViewModelLoader,
            formDefinitionOf("user-task-1")
        )
        assertThat(missingFields).isEmpty()
    }

    @Test
    fun `should find missing fields when ViewModel has extra fields`() {
        val testViewModelLoader = TestViewModelLoader()
        val missingFields = onStartUpViewModelValidator.validateViewModel(
            testViewModelLoader,
            formDefinitionOf("user-task-2")
        )
        assertThat(missingFields).isNotEmpty()
        assertThat(missingFields).contains("age")
    }

    @Test
    fun `should not find missing fields when all Submission fields match form`() {
        val testSubmissionHandler = TestStartFormSubmissionHandler()
        val missingFields = onStartUpViewModelValidator.validateStartFormSubmission(
            testSubmissionHandler,
            formDefinitionOf("user-task-1")
        )
        assertThat(missingFields).isEmpty()
    }

    @Test
    fun `should find missing fields when Submission has extra fields`() {
        val testSubmissionHandler = TestStartFormSubmissionHandler()
        val missingFields = onStartUpViewModelValidator.validateStartFormSubmission(
            testSubmissionHandler,
            formDefinitionOf("user-task-2")
        )
        assertThat(missingFields).isNotEmpty()
        assertThat(missingFields).contains("age")
    }

    @Test
    fun `should throw exception when form could not be found`() {
        val viewModelLoader: ViewModelLoader<*> = Mockito.mock()
        whenever(viewModelLoader.getFormName()).thenReturn("I do not exist")

        val exception = assertThrows<NoSuchElementException> {
            onStartUpViewModelValidator.validateViewModelLoader(
                viewModelLoader
            )
        }
        assertThat(exception.message).contains("Could not find form [I do not exist] declared in class com.ritense.formviewmodel.viewmodel.ViewModelLoader\$MockitoMock")
    }

    @Test
    fun `should log validation errors to stdout`() {
        // Redirect System.err to capture what is printed
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        mockViewModelLoader("user-task-1", true)
        onStartUpViewModelValidator.validate()

        mockViewModelLoader("user-task-2", false)
        onStartUpViewModelValidator.validate()

        // Reset System.err
        System.setOut(System.out)

        // Get the captured output
        val printedStackTrace = outputStream.toString()

        // Verify if the expected stack trace was printed
        assertTrue(
            printedStackTrace.contains(
                "The following properties are missing in the view model for form (user-task-2): [age, dataContainer.nestedData]"
            )
        )
        assertThat(printedStackTrace).contains(
            "The following properties are missing in the start form submission for form (user-task-2): [age, dataContainer.nestedData]"
        )
    }

    private fun mockViewModelLoader(formName: String, isValid: Boolean): ViewModelLoader<ViewModel> {
        val viewModel = TestViewModel()
        if (isValid) {
            whenever(formIoFormDefinitionService.getFormDefinitionByName(formName)).doReturn(getValidFormDefinition())
        } else {
            whenever(formIoFormDefinitionService.getFormDefinitionByName(formName)).doReturn(getInvalidFormDefinition())
        }
        whenever(viewModelLoader.getFormName()).thenReturn(formName)
        whenever(viewModelLoader.getViewModelType()).thenReturn(viewModel::class as KClass<ViewModel>)
        whenever(formViewModelStartFormSubmissionHandlerFactory.getHandler(formName)).thenReturn(
            TestStartFormSubmissionHandler()
        )
        return viewModelLoader
    }

    private fun getValidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-1"))

    private fun getInvalidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-2"))
}
