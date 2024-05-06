package com.ritense.formviewmodel.domain.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.domain.ViewModel
import com.ritense.formviewmodel.domain.ViewModelLoader
import com.ritense.formviewmodel.web.rest.TestViewModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import java.util.Optional
import kotlin.reflect.KClass

class OnStartUpViewModelValidatorTest : BaseTest() {

    @Mock
    private lateinit var formIoFormDefinitionService: FormIoFormDefinitionService

    @Mock
    private lateinit var context: ApplicationContext

    @InjectMocks
    private lateinit var onStartUpViewModelValidator: OnStartUpViewModelValidator

    @BeforeEach
    fun setUp() {
        formIoFormDefinitionService = mock()
        context = mock()
        onStartUpViewModelValidator = OnStartUpViewModelValidator(formIoFormDefinitionService, context)
    }

    @Test
    fun `validateAllViewModels should throw IllegalArgumentException when a ViewModelLoader is not valid`() {
        // Mocking ViewModelLoader
        val viewModelLoader1 = mockViewModelLoader("user-task-1", true)
        val viewModelLoader2 = mockViewModelLoader("user-task-2", false)

        whenever(context.getBeansOfType(ViewModelLoader::class.java))
            .thenReturn(mapOf("viewModelLoader1" to viewModelLoader1, "viewModelLoader2" to viewModelLoader2))


        // Mocking FormIoFormDefinitionService
        whenever(formIoFormDefinitionService.getFormDefinitionByName("user-task-1")).thenReturn(getValidFormDefinition())
        whenever(formIoFormDefinitionService.getFormDefinitionByName("user-task-2")).thenReturn(getInvalidFormDefinition())

        // Test
        assertThrows<IllegalArgumentException> {
            onStartUpViewModelValidator.validateAllViewModels()
        }
    }

    private fun mockViewModelLoader(formName: String, isValid: Boolean): ViewModelLoader<ViewModel> {
        val viewModelLoader = mock(ViewModelLoader::class.java) as ViewModelLoader<ViewModel>
        val viewModel = TestViewModel()
        if (!isValid) {
            whenever(formIoFormDefinitionService.getFormDefinitionByName(any())).doReturn(getValidFormDefinition())
        } else {
            whenever(formIoFormDefinitionService.getFormDefinitionByName(any())).doReturn(getInvalidFormDefinition())
        }
        whenever(viewModelLoader.getFormName()).thenReturn(formName)
        whenever(viewModelLoader.getViewModelType()).thenReturn(viewModel::class as KClass<ViewModel>)
        return viewModelLoader
    }

    private fun getValidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-1"))

    private fun getInvalidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-2"))
}
