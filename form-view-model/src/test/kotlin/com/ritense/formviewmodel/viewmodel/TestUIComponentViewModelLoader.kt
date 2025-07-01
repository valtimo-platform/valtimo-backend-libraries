package com.ritense.formviewmodel.viewmodel

import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import com.ritense.valtimo.operaton.domain.OperatonTask

class TestUIComponentViewModelLoader(
    private val componentKey: String = "my-component",
    ) : ViewModelLoader<TestViewModel> {

    override fun load(task: OperatonTask?): TestViewModel {
        return TestViewModel()
    }

    override fun supports(processLink: ProcessLink): Boolean {
        return (processLink as? UIComponentProcessLink)?.componentKey == componentKey
    }
}