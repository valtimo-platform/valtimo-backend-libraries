package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.submission.FormViewModelSubmissionHandlerFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FormViewModelSubmissionHandlerFactoryTest : BaseTest() {

    private lateinit var formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory

    @BeforeEach
    fun setUp() {
        formViewModelSubmissionHandlerFactory = FormViewModelSubmissionHandlerFactory(
            listOf(TestSubmissionHandler())
        )
    }

    @Test
    fun `should create submission handler`() {
        val handler = formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler("test")
        assertThat(handler).isInstanceOf(TestSubmissionHandler::class.java)
    }

    @Test
    fun `should return null when no submission handler found`() {
        val handler = formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler("doesNotExist")
        assertThat(handler).isNull()
    }
}