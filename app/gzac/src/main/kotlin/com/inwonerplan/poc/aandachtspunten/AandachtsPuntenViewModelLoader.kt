package com.inwonerplan.poc.aandachtspunten

import com.inwonerplan.api.StamtabellenApi
import com.ritense.formviewmodel.domain.ViewModelLoader

class AandachtsPuntenViewModelLoader: ViewModelLoader<AandachtsPuntenViewModel> {

    override fun onLoad(taskInstanceId: String): AandachtsPuntenViewModel {
        return AandachtsPuntenViewModel(
            StamtabellenApi().getAandachtspunten(),
            null
        )
    }

    override fun supports(formName: String): Boolean {
        return formName == "form_aandachtspunt"
    }

    override fun getFormName(): String {
        return "form_aandachtspunt"
    }

}