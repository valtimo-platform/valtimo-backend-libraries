package com.inwonerplan.poc.aandachtspunten

import com.inwonerplan.api.StamtabellenApi
import com.ritense.formviewmodel.viewmodel.ViewModelLoader

class AandachtsPuntenViewModelLoader: ViewModelLoader<AandachtsPuntenViewModel> {

    override fun load(taskInstanceId: String): AandachtsPuntenViewModel {
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