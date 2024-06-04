package com.inwonerplan.poc.aanbod

import com.inwonerplan.api.StamtabellenApi
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.valtimo.camunda.domain.CamundaTask

class AanbodViewModelLoader: ViewModelLoader<AanbodViewModel> {

    override fun load(task: CamundaTask?): AanbodViewModel {
        return AanbodViewModel(
            emptyList(),
            StamtabellenApi().getAandachtspunten()
        )
    }

    override fun getFormName(): String {
        return "form_aanbod"
    }

}