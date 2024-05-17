package com.inwonerplan.poc.aanbod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.model.Aanbod
import com.inwonerplan.model.AanbodActiviteit
import com.inwonerplan.model.Aandachtspunt
import com.inwonerplan.model.Subdoel
import com.ritense.formviewmodel.viewmodel.ViewModel

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodViewModel(
    val aanbodGrid: List<AanbodGridRow>,
    val aandachtspunten: List<Aandachtspunt>
) : ViewModel {

    override fun update(): ViewModel {
        println("Updating")
        val copy = this.copy(
            aanbodGrid = aanbodGrid.map {
                println(it.subdoel)
                val aanbiedingen = if(!it.subdoel.isNullOrBlank()) {
                    StamtabellenApi().getAanbodWithSubdoel(it.subdoel)
                } else null
                it.copy(
                    aandachtspuntOriginal = it.aandachtspunt,
                    subdoelOriginal = it.subdoel,
                    subdoelen = it.aandachtspunt?.let { getSubdoelenForAandachtspunten(it) },
                    aanbiedingenGrid = it.aanbiedingenGrid?.map {
                        if(it.aanbod != it.aanbodOriginal && !it.aanbod.isNullOrBlank()) {
                            it.copy(
                                aanbiedingen = aanbiedingen,
                                aanbodOriginal = it.aanbod,
                                activiteiten = it.aanbod?.let { aanbod -> getActiviteitenForAanbod(aanbiedingen!!, aanbod) },
                                activiteit = null
                            )
                        } else {
                            it.copy()
                        }
                    }
                )
            }
        )
        return copy
    }

    fun getActiviteitenForAanbod(aanbiedingen: List<Aanbod>, aanbod: String): List<AanbodActiviteit>? {
        val aanbod = aanbiedingen.find { it.id.toString() == aanbod }
        requireNotNull(aanbod) { "No aanbod found for $aanbod" }
        return aanbod.aanbodActiviteiten
    }

    fun getSubdoelenForAandachtspunten(aandachtsPunt: String) =
        StamtabellenApi().getSubdoelen().filter { subdoel ->
            subdoel.aandachtspunten!!.find { subdoelAandachtsPunt ->
                subdoelAandachtsPunt.id!!.toString() == aandachtsPunt
            } != null
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodGridRow(
    val aandachtspuntOriginal: String?,
    val aandachtspunt: String?,
    val subdoelOriginal: String?,
    val subdoel: String?,
    val subdoelen: List<Subdoel>?,
    val aanbiedingenGrid: List<AanbodRow>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodRow(
    val aanbodOriginal: String?,
    val aanbod: String?,
    val status: String?,
    val aanbiedingen: List<Aanbod>?,
    val activiteiten: List<AanbodActiviteit>?,
    val activiteit: String?
)