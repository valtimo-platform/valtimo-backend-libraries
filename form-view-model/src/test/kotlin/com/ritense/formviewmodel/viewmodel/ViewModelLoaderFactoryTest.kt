package com.ritense.formviewmodel.viewmodel

import com.ritense.formviewmodel.BaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ViewModelLoaderFactoryTest : BaseTest() {

    private lateinit var viewModelLoaderFactory: ViewModelLoaderFactory

    @BeforeEach
    fun setUp() {
        viewModelLoaderFactory = ViewModelLoaderFactory(listOf(TestViewModelLoader()))
    }

    @Test
    fun `should create view model`() {
        val viewModelLoader = viewModelLoaderFactory.getViewModelLoader("test")
        assertThat(viewModelLoader).isInstanceOf(TestViewModelLoader::class.java)
    }

}