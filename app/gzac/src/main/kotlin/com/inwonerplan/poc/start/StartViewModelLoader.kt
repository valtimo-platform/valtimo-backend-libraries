package com.inwonerplan.poc.start

import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.valtimo.camunda.domain.CamundaTask

class StartViewModelLoader: ViewModelLoader<StartViewModel> {

    override fun load(task: CamundaTask?): StartViewModel {
        return StartViewModel(
            test = "test"
        )
    }

    override fun getFormName(): String {
        return "empty-form"
    }

}