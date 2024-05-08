package com.inwonerplan.poc.aanbod

import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.domain.ViewModelLoader

class AanbodViewModelLoader: ViewModelLoader<AanbodViewModel> {

    override fun onLoad(taskInstanceId: String): AanbodViewModel {
        val subdoel = POCSubmissions.subdoelen!!

        val aanbodGrid = subdoel.subdoelenGrid.map {
            AanbodGridRow(
                it.aandachtspunt,
                resolveSubdoelNaam(it.subdoel!!),
                null
            )
        }

        return AanbodViewModel(
            StamtabellenApi().getAanbod(),
            null,
            aanbodGrid
        )
    }

    fun resolveSubdoelNaam(subdoelId: String) =
        StamtabellenApi().getSubdoelen().find { it.uuid!!.toString() == subdoelId}!!.naam!!

    override fun supports(formName: String): Boolean {
        return formName == "form_aanbod"
    }

    override fun getFormName(): String {
        return "form_aanbod"
    }

}