package com.inwonerplan.poc.aanbod

import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.viewmodel.ViewModelLoader

class AanbodViewModelLoader: ViewModelLoader<AanbodViewModel> {

    override fun load(taskInstanceId: String): AanbodViewModel {
        return AanbodViewModel(
            emptyList(),
            StamtabellenApi().getAandachtspunten()
        )
    }

    override fun supports(formName: String): Boolean {
        return formName == getFormName()
    }

    override fun getFormName(): String {
        return "form_aanbod"
    }

}