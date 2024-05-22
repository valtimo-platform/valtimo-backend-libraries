package com.inwonerplan.poc.aanbod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.model.Aanbod
import com.inwonerplan.model.AanbodActiviteit
import com.inwonerplan.model.Aandachtspunt
import com.inwonerplan.model.Subdoel
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodViewModel(
    val aanbodGrid: List<AanbodGridRow>,
    val aandachtspunten: List<Aandachtspunt>
) : ViewModel, Submission {

    override fun update(task: CamundaTask): ViewModel {
        println("Updating")
        val copy = this.copy(
            aanbodGrid = aanbodGrid.map {
                val aanbiedingen = if(!it.subdoel.isNullOrBlank()) {
                    StamtabellenApi().getAanbodWithSubdoel(it.subdoel)
                } else null
                it.copy(
                    subdoelen = it.aandachtspunt?.let { getSubdoelenForAandachtspunten(it) },
                    aanbiedingenGrid = it.aanbiedingenGrid?.map { aanbodRow ->
                        if(it.subdoel == null) {
                            AanbodRow(
                                aanbod = null,
                                status = null,
                                aanbiedingen = null,
                                activiteiten = null,
                                activiteit = null
                            )
                        } else if(!aanbodRow.aanbod.isNullOrBlank()) {
                            aanbodRow.copy(
                                aanbiedingen = aanbiedingen,
                                activiteiten = aanbodRow.aanbod?.let { aanbod -> getActiviteitenForAanbod(aanbiedingen!!, aanbod) },
                                activiteit = null
                            )
                        } else {
                            aanbodRow.copy(
                                aanbiedingen = aanbiedingen,
                            )
                        }
                    }
                )
            }
        )
        return copy
    }

    private fun getActiviteitenForAanbod(aanbiedingen: List<Aanbod>, aanbod: String): List<AanbodActiviteit>? {
        val aanbod = aanbiedingen.find { it.id.toString() == aanbod }
        requireNotNull(aanbod) { "No aanbod found for $aanbod" }
        return aanbod.aanbodActiviteiten
    }

    private fun getSubdoelenForAandachtspunten(aandachtsPunt: String) =
        StamtabellenApi().getSubdoelen().filter { subdoel ->
            subdoel.aandachtspunten!!.find { subdoelAandachtsPunt ->
                subdoelAandachtsPunt.id!!.toString() == aandachtsPunt
            } != null
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodGridRow(
    val aandachtspunt: String?,
    val subdoel: String?,
    val subdoelen: List<Subdoel>?,
    val aanbiedingenGrid: List<AanbodRow>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodRow(
    val aanbod: String?,
    val status: String?,
    val aanbiedingen: List<Aanbod>?,
    val activiteiten: List<AanbodActiviteit>?,
    val activiteit: String?
)