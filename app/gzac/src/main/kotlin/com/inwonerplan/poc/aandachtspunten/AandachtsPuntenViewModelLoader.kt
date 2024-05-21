package com.inwonerplan.poc.aandachtspunten

import com.inwonerplan.api.StamtabellenApi
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.valtimo.camunda.domain.CamundaTask

class AandachtsPuntenViewModelLoader: ViewModelLoader<AandachtsPuntenViewModel> {

    override fun load(task: CamundaTask): AandachtsPuntenViewModel {
        return AandachtsPuntenViewModel(
            StamtabellenApi().getAandachtspunten(),
            null
        )
    }

    override fun supports(formName: String): Boolean {
        return formName == getFormName()
    }

    override fun getFormName(): String {
        return "form_aandachtspunt"
    }

}