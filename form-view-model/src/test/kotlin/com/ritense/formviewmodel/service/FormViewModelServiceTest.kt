package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.json.MapperSingleton
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FormViewModelServiceTest : BaseTest() {

    private lateinit var formViewModelService: FormViewModelService

    @BeforeEach
    fun setUp() {
        formViewModelService = FormViewModelService(
            objectMapper = MapperSingleton.get()
        )
    }

    @Test
    fun `should parse ViewModel`() {
        val submission = submission()
        val viewModelInstance = formViewModelService.parseViewModel(
            submission = submission,
            viewModelType = TestViewModel::class
        )
        assertThat(viewModelInstance).isInstanceOf(TestViewModel::class.java)
        val viewModelInstanceCasted = viewModelInstance as TestViewModel
        assertThat(viewModelInstanceCasted.test).isEqualTo("test")
    }

    @Test
    fun `should not parse ViewModel of wrong type`() {
        val submission = submission()
        assertThrows<IllegalArgumentException> {
            formViewModelService.parseViewModel(
                submission = submission,
                viewModelType = RandomViewModel::class
            )
        }
    }

    fun submission(): ObjectNode = MapperSingleton.get().createObjectNode()
        .put("test", "test")
        .put("test2", "test2")

    data class RandomViewModel(
        val custom: String
    ) : ViewModel {

        override fun update(task: CamundaTask): ViewModel {
            return this
        }
    }

}