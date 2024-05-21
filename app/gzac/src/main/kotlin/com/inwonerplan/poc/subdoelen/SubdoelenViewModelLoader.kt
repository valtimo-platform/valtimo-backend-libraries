package com.inwonerplan.poc.subdoelen

import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.valtimo.camunda.domain.CamundaTask

class SubdoelenViewModelLoader: ViewModelLoader<SubdoelenViewModel> {

    override fun load(task: CamundaTask): SubdoelenViewModel {
        val aandachtsPunten = POCSubmissions.aandachtsPunten!!

        val subdoelenGrid = aandachtsPunten.aandachtspuntenGrid!!.map {
            SubdoelenGridRow(
                it.aandachtspunt,
                it.aandachtspunt,
                getSubdoelenForAandachtspunten(it.aandachtspunt)
            )
        }

        return SubdoelenViewModel(
            subdoelenGrid,
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
        return formName == getFormName()
    }

    override fun getFormName(): String {
        return "form_subdoelen"
    }

}