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
                it.aandachtspunt,
                it.subdoel!!,
                it.subdoel,
                getSubdoelenForAandachtspunten(it.aandachtspunt),
                null
            )
        }

        return AanbodViewModel(
            StamtabellenApi().getAanbod(),
            aanbodGrid,
            StamtabellenApi().getAandachtspunten()
        )
    }

    fun getSubdoelenForAandachtspunten(aandachtsPunt: String) =
        StamtabellenApi().getSubdoelen().filter { subdoel ->
            subdoel.aandachtspunten!!.find { subdoelAandachtsPunt ->
                subdoelAandachtsPunt.id!!.toString() == aandachtsPunt
            } != null
        }

    override fun supports(formName: String): Boolean {
        return formName == "form_aanbod"
    }

    override fun getFormName(): String {
        return "form_aanbod"
    }

}