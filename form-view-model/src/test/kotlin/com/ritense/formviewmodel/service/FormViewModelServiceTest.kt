package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.json.MapperSingleton
import com.ritense.formviewmodel.viewmodel.TestViewModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FormViewModelServiceTest {

    private lateinit var formViewModelService: FormViewModelService

    @BeforeEach
    fun setUp() {
        formViewModelService = FormViewModelService(
            objectMapper = MapperSingleton.get()
        )
    }

    @Test
    fun `should parse submission to ViewModel of type`() {
        val submission = submission()
        val viewModelInstance = formViewModelService.parseViewModel(
            submission = submission,
            viewModelType = TestViewModel::class
        )
        assertThat(viewModelInstance).isInstanceOf(TestViewModel::class.java)
        val viewModelInstanceCasted = viewModelInstance as TestViewModel
        assertThat(viewModelInstanceCasted.test).isEqualTo("test")
    }

    fun submission(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("test", "test")
        .put("test2", "test2")
}