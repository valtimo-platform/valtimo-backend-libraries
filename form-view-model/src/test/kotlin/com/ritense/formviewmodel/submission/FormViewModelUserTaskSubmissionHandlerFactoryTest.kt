package com.ritense.formviewmodel.submission

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
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FormViewModelUserTaskSubmissionHandlerFactoryTest(
    @Mock private val formDefinitionService: FormDefinitionService,
) : BaseTest() {

    private lateinit var formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory
    private val testUserTaskSubmissionHandler = TestUserTaskSubmissionHandler()

    @BeforeEach
    fun setUp() {
        formViewModelUserTaskSubmissionHandlerFactory = FormViewModelUserTaskSubmissionHandlerFactory(
            listOf(testUserTaskSubmissionHandler),
            formDefinitionService
        )
    }

    @Test
    fun `should create submission handler for form validation`() {
        val handler = formViewModelUserTaskSubmissionHandlerFactory.getHandlerForFormValidation(testUserTaskSubmissionHandler.formName)
        assertThat(handler).isInstanceOf(TestUserTaskSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found for form validation`() {
        val handler = formViewModelUserTaskSubmissionHandlerFactory.getHandlerForFormValidation("doesNotExist")
        assertThat(handler).isNull()
    }

    @Test
    fun `should create submission handler for processLink`() {
        val processsLink = mock<FormProcessLink?>().apply {
            whenever(this.formDefinitionId).thenReturn(testUserTaskSubmissionHandler.formDefinitionId)
        }
        val handler = formViewModelUserTaskSubmissionHandlerFactory.getHandler(processsLink)
        assertThat(handler).isInstanceOf(TestUserTaskSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found for processLink`() {
        val processsLink = mock<FormProcessLink?>().apply {
            whenever(this.formDefinitionId).thenReturn(UUID.randomUUID())
        }
        val handler = formViewModelUserTaskSubmissionHandlerFactory.getHandler(processsLink)
        assertThat(handler).isNull()
    }
}