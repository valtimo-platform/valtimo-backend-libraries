package com.ritense.formviewmodel.submission

import com.ritense.formviewmodel.BaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FormViewModelUserTaskSubmissionHandlerFactoryTest : BaseTest() {

    private lateinit var formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory

    @BeforeEach
    fun setUp() {
        formViewModelUserTaskSubmissionHandlerFactory = FormViewModelUserTaskSubmissionHandlerFactory(
            listOf(TestUserTaskSubmissionHandler())
        )
    }

    @Test
    fun `should create submission handler`() {
        val handler = formViewModelUserTaskSubmissionHandlerFactory.getHandler("test")
        assertThat(handler).isInstanceOf(TestUserTaskSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found`() {
        val handler = formViewModelUserTaskSubmissionHandlerFactory.getHandler("doesNotExist")
        assertThat(handler).isNull()
    }
}