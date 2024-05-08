package com.inwonerplan.poc.subdoelen

import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.domain.ViewModelLoader

class SubdoelenViewModelLoader: ViewModelLoader<SubdoelenViewModel> {

    override fun onLoad(taskInstanceId: String): SubdoelenViewModel {
        val aandachtsPunten = POCSubmissions.aandachtsPunten!!
        val aandachtsPuntNaam = resolveAandachtsPuntNaam(aandachtsPunten.aandachtspunt!!)

        val subdoelenGrid = aandachtsPunten.aandachtspuntenGrid!!.map {
            SubdoelenGridRow(
                resolveAandachtsPuntNaam(it.aandachtspunt)
            )
        }

        return SubdoelenViewModel(
            aandachtsPuntNaam,
            subdoelenGrid,
            StamtabellenApi().getSubdoelen().filter { subdoel ->
                subdoel.aandachtspunten!!.find {
                    it.id!!.toString() == aandachtsPunten.aandachtspunt
                } != null
            },
            null
        )
    }

    fun resolveAandachtsPuntNaam(id: String) =
        StamtabellenApi().getAandachtspunten().find { it.id!!.toString() == id }!!.naamAandachtsPunt!!

    override fun supports(formName: String): Boolean {
        return formName == "form_subdoelen"
    }

    override fun getFormName(): String {
        return "form_subdoelen"
    }

}