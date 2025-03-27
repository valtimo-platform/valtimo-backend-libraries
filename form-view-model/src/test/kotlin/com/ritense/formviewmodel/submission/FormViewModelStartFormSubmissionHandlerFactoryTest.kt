package com.ritense.formviewmodel.submission

import com.ritense.form.domain.FormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.FormDefinitionService
import com.ritense.formviewmodel.BaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FormViewModelStartFormSubmissionHandlerFactoryTest(
    @Mock private val formDefinitionService: FormDefinitionService,
) : BaseTest() {

    private lateinit var formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory
    private val testStartFormSubmissionHandler = TestStartFormSubmissionHandler(formDefinitionService = formDefinitionService)

    @BeforeEach
    fun setUp() {
        formViewModelStartFormSubmissionHandlerFactory = FormViewModelStartFormSubmissionHandlerFactory(
            listOf(testStartFormSubmissionHandler),
            formDefinitionService
        )
    }

    @Test
    fun `should create submission handler for form validation`() {
        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandlerForFormValidation(testStartFormSubmissionHandler.userTaskFormName)
        assertThat(handler).isInstanceOf(TestStartFormSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found for form validation`() {
        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandlerForFormValidation("doesNotExist")
        assertThat(handler).isNull()
    }

    @Test
    fun `should create submission handler for processLink`() {
        val formDefinitionId = UUID.randomUUID()
        val processsLink = mock<FormProcessLink?>().apply {
            whenever(this.formDefinitionId).thenReturn(formDefinitionId)
        }
        val formDefinition = mock<FormDefinition?>().apply {
            whenever(this.name).thenReturn(testStartFormSubmissionHandler.userTaskFormName)
        }
        whenever(formDefinitionService.getFormDefinitionById(formDefinitionId)).thenReturn(Optional.of(formDefinition))

        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandler(processsLink)
        assertThat(handler).isInstanceOf(TestStartFormSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found for processLink`() {
        val processsLink = mock<FormProcessLink?>().apply {
            whenever(this.formDefinitionId).thenReturn(UUID.randomUUID())
        }
        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandler(processsLink)
        assertThat(handler).isNull()
    }
}