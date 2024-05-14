package com.ritense.formviewmodel.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.event.TestSubmissionHandler
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import org.assertj.core.api.Assertions.assertThat
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
    fun `should be valid ViewModel`() {
        val testViewModelLoader = TestViewModelLoader()
        val missingFields = onStartUpViewModelValidator.validateViewModel(
            testViewModelLoader,
            formDefinitionOf("user-task-1")
        )
        assertThat(missingFields).isEmpty()
    }

    @Test
    fun `should not be valid ViewModel`() {
        val testViewModelLoader = TestViewModelLoader()
        val missingFields = onStartUpViewModelValidator.validateViewModel(
            testViewModelLoader,
            formDefinitionOf("user-task-2")
        )
        assertThat(missingFields).isNotEmpty()
        assertThat(missingFields).contains("age")
    }

    @Test
    fun `should be valid Submission`() {
        val testSubmissionHandler = TestSubmissionHandler()
        val missingFields = onStartUpViewModelValidator.validateSubmission(
            testSubmissionHandler,
            formDefinitionOf("user-task-1")
        )
        assertThat(missingFields).isEmpty()
    }

    @Test
    fun `should not be valid Submission`() {
        val testSubmissionHandler = TestSubmissionHandler()
        val missingFields = onStartUpViewModelValidator.validateSubmission(
            testSubmissionHandler,
            formDefinitionOf("user-task-2")
        )
        assertThat(missingFields).isNotEmpty()
        assertThat(missingFields).contains("age")
    }

    // Example ViewModels
    data class Person(val name: String, val address: Address)
    data class Address(val street: String, val city: City)
    data class City(val name: String, val code: Int)

    @Test
    fun `should extract all ViewModel field names`() {
        onStartUpViewModelValidator.extractFieldNames(Person::class).let {
            assertTrue(it.contains("address.city.code"))
            assertTrue(it.contains("address.city.name"))
            assertTrue(it.contains("address.street"))
            assertTrue(it.contains("name"))
        }
    }

    data class MySubmission(val name: String, val address: Address) : Submission

    @Test
    fun `should extract all field names for Submission`() {
        onStartUpViewModelValidator.extractFieldNames(MySubmission::class).let {
            assertTrue(it.contains("name"))
            assertTrue(it.contains("address.city.code"))
            assertTrue(it.contains("address.city.name"))
        }
    }

    @Test
    fun `should validate`() {
        // Redirect System.err to capture what is printed
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        mockViewModelLoader("user-task-1", true)
        mockViewModelLoader("user-task-2", false)

        onStartUpViewModelValidator.validate()

        // Reset System.err
        System.setOut(System.out)

        // Get the captured output
        val printedStackTrace = outputStream.toString()

        // Verify if the expected stack trace was printed
        assertTrue(
            printedStackTrace.contains(
                "The following properties are missing in the view model for form (user-task-2): [dataContainer, age, nestedData]"
            )
        )

        assertTrue(
            printedStackTrace.contains(
                "The following properties are missing in the submission for form (user-task-2): [dataContainer, age, nestedData]"
            )
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
        whenever(formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(formName)).thenReturn(
            TestSubmissionHandler()
        )
        return viewModelLoader
    }

    private fun getValidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-1"))

    private fun getInvalidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-2"))
}
