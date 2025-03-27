package com.ritense.formviewmodel.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.TestStartFormSubmissionHandler
import com.ritense.formviewmodel.viewmodel.FormViewModelLoader
import com.ritense.formviewmodel.viewmodel.TestFormViewModelLoader
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Optional
import kotlin.reflect.KClass
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class OnStartUpFormViewModelValidatorTest(
    @Mock private val formIoFormDefinitionService: FormIoFormDefinitionService,
    @Mock private val viewModelLoader: FormViewModelLoader<ViewModel>,
    @Mock private val formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory,
    @Mock private val formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory,
) : BaseTest() {
    private lateinit var onStartUpViewModelValidator: OnStartUpViewModelValidator


    @BeforeEach
    fun setUp() {
        onStartUpViewModelValidator = OnStartUpViewModelValidator(
            formIoFormDefinitionService,
            listOf(viewModelLoader),
            formViewModelStartFormSubmissionHandlerFactory,
            formViewModelUserTaskSubmissionHandlerFactory
        )
    }

    @Test
    fun `should not find missing fields when all ViewModel fields match form`() {
        val testViewModelLoader = TestFormViewModelLoader()
        val missingFields = onStartUpViewModelValidator.validateViewModel(
            testViewModelLoader,
            formDefinitionOf("user-task-1")
        )
        assertThat(missingFields).isEmpty()
    }

    @Test
    fun `should find missing fields when ViewModel has extra fields`() {
        val testViewModelLoader = TestFormViewModelLoader()
        val missingFields = onStartUpViewModelValidator.validateViewModel(
            testViewModelLoader,
            formDefinitionOf("user-task-2")
        )
        assertThat(missingFields).isNotEmpty()
        assertThat(missingFields).contains("age")
    }

    @Test
    fun `should not find missing fields when all Submission fields match form`() {
        val testSubmissionHandler = TestStartFormSubmissionHandler(formDefinitionService = formIoFormDefinitionService)
        val missingFields = onStartUpViewModelValidator.validateStartFormSubmission(
            testSubmissionHandler,
            formDefinitionOf("user-task-1")
        )
        assertThat(missingFields).isEmpty()
    }

    @Test
    fun `should find missing fields when Submission has extra fields`() {
        val testSubmissionHandler = TestStartFormSubmissionHandler(formDefinitionService = formIoFormDefinitionService)
        val missingFields = onStartUpViewModelValidator.validateStartFormSubmission(
            testSubmissionHandler,
            formDefinitionOf("user-task-2")
        )
        assertThat(missingFields).isNotEmpty()
        assertThat(missingFields).contains("age")
    }

    @Test
    fun `should throw exception when form could not be found`() {
        val viewModelLoader: FormViewModelLoader<*> = Mockito.mock()
        whenever(viewModelLoader.getFormName()).thenReturn("I do not exist")

        val exception = assertThrows<NoSuchElementException> {
            onStartUpViewModelValidator.validateViewModelLoader(
                viewModelLoader
            )
        }
        assertThat(exception.message).contains("Could not find form [I do not exist] declared in class com.ritense.formviewmodel.viewmodel.FormViewModelLoader\$MockitoMock")
    }

    @Test
    fun `should log validation errors to stdout`() {
        val originalOut = System.out
        // Redirect System.err to capture what is printed
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        mockViewModelLoader("user-task-1", true)
        onStartUpViewModelValidator.validate()

        mockViewModelLoader("user-task-2", false)
        onStartUpViewModelValidator.validate()

        // Reset System.err
        System.setOut(originalOut)

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
        whenever(formViewModelStartFormSubmissionHandlerFactory.getHandlerForFormValidation(formName)).thenReturn(
            TestStartFormSubmissionHandler(formDefinitionService = formIoFormDefinitionService)
        )
        return viewModelLoader
    }

    private fun getValidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-1"))

    private fun getInvalidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-2"))
}
