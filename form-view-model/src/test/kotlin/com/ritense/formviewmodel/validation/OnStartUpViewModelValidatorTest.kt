package com.ritense.formviewmodel.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.event.TestSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
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

    @Mock
    private lateinit var formIoFormDefinitionService: FormIoFormDefinitionService

    @InjectMocks
    private lateinit var onStartUpViewModelValidator: OnStartUpViewModelValidator

    private lateinit var viewModelLoaders: List<ViewModelLoader<*>>

    private lateinit var viewModelLoader: ViewModelLoader<ViewModel>

    private lateinit var formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory

    @BeforeEach
    fun setUp() {
        formIoFormDefinitionService = mock()
        viewModelLoader = mock(ViewModelLoader::class.java) as ViewModelLoader<ViewModel>
        viewModelLoaders = listOf(viewModelLoader)
        formViewModelSubmissionHandlerFactory = mock()
        onStartUpViewModelValidator = OnStartUpViewModelValidator(
            formIoFormDefinitionService,
            viewModelLoaders,
            formViewModelSubmissionHandlerFactory
        )
    }

    @Test
    fun `validateAllViewModels should print stack trace`() {
        // Redirect System.err to capture what is printed
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        mockViewModelLoader("user-task-1", true)
        mockViewModelLoader("user-task-2", false)

        onStartUpViewModelValidator.validateAllViewModels()

        // Reset System.err
        System.setOut(System.out)

        // Get the captured output
        val printedStackTrace = outputStream.toString()

        // Verify if the expected stack trace was printed
        assertTrue(printedStackTrace.contains(
            "The following properties are missing in the view model for form (user-task-2): [age]")
        )

        assertTrue(printedStackTrace.contains(
            "The following properties are missing in the submission for form (user-task-2): [age]")
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
        whenever(formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(formName)).thenReturn(TestSubmissionHandler())
        return viewModelLoader
    }

    private fun getValidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-1"))

    private fun getInvalidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-2"))
}
