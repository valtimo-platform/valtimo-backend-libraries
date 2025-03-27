package com.ritense.formviewmodel.viewmodel

import com.ritense.form.domain.FormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.FormDefinitionService
import com.ritense.formviewmodel.BaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

internal class ViewModelLoaderFactoryTest : BaseTest() {

    private lateinit var viewModelLoaderFactory: ViewModelLoaderFactory
    private lateinit var formProcessLink: FormProcessLink
    @BeforeEach
    fun setUp() {
        val formDefinitionId = UUID.randomUUID()
        formProcessLink = mock<FormProcessLink>().apply {
            whenever(this.formDefinitionId).thenReturn(formDefinitionId)
        }

        val formDefinition = mock<FormDefinition>().apply {
            whenever(this.name).thenReturn("test")
        }
        val formDefinitionService: FormDefinitionService = mock<FormDefinitionService>().apply {
            whenever(this.getFormDefinitionById(formDefinitionId)).thenReturn(Optional.of(formDefinition))
        }
        viewModelLoaderFactory = ViewModelLoaderFactory(listOf(TestFormViewModelLoader()), formDefinitionService)
    }

    @Test
    fun `should create view model`() {
        val viewModelLoader = viewModelLoaderFactory.getViewModelLoader(formProcessLink)
        assertThat(viewModelLoader).isInstanceOf(TestFormViewModelLoader::class.java)
    }

}