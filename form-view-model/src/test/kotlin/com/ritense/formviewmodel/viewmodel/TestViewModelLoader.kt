package com.ritense.formviewmodel.viewmodel

class TestViewModelLoader : ViewModelLoader<TestViewModel> {
    override fun load(taskInstanceId: String): TestViewModel {
        return TestViewModel()
    }

    override fun supports(formName: String): Boolean {
        return formName == getFormName()
    }

    override fun getFormName(): String = "formName"
}