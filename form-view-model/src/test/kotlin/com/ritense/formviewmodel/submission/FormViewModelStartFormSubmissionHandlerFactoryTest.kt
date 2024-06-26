package com.ritense.formviewmodel.submission

import com.ritense.formviewmodel.BaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FormViewModelStartFormSubmissionHandlerFactoryTest : BaseTest() {

    private lateinit var formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory

    @BeforeEach
    fun setUp() {
        formViewModelStartFormSubmissionHandlerFactory = FormViewModelStartFormSubmissionHandlerFactory(
            listOf(TestStartFormSubmissionHandler())
        )
    }

    @Test
    fun `should create submission handler`() {
        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandler("test")
        assertThat(handler).isInstanceOf(TestStartFormSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found`() {
        val handler = formViewModelStartFormSubmissionHandlerFactory.getHandler("doesNotExist")
        assertThat(handler).isNull()
    }
}