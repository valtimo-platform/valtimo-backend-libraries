package com.ritense.formviewmodel.viewmodel

import com.ritense.valtimo.operaton.domain.OperatonTask

class TestFormViewModelLoader(
    private val formName: String = "test",
) : FormViewModelLoader<TestViewModel>() {
    override fun load(task: OperatonTask?): TestViewModel {
        return TestViewModel()
    }

    override fun getFormName(): String = formName
}