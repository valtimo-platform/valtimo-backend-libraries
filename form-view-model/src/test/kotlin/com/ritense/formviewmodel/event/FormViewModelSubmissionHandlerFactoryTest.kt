package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.BaseTest
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
}