package com.inwonerplan.poc.aanbod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.model.Aanbod
import com.inwonerplan.model.AanbodActiviteit
import com.inwonerplan.model.Aandachtspunt
import com.inwonerplan.model.Subdoel
import com.ritense.formviewmodel.domain.ViewModel

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodViewModel(
    val aanbiedingen: List<Aanbod>,
    val aanbodGrid: List<AanbodGridRow>,
    val aandachtspunten: List<Aandachtspunt>
) : ViewModel {

    override fun update(): ViewModel {
        println("Updating")
        return this.copy(
            aanbodGrid = aanbodGrid.map {
                it.copy(
                    aandachtspuntOriginal = it.aandachtspunt,
                    subdoelOriginal = it.subdoel,
                    subdoelen = getSubdoelenForAandachtspunten(it.aandachtspunt),
                    aanbiedingenGrid = it.aanbiedingenGrid?.map {
                        it.copy(
                            aanbodOriginal = it.aanbod
                        )
                    }
                )
            }
        )
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
    val aandachtspuntOriginal: String,
    val aandachtspunt: String,
    val subdoelOriginal: String?,
    val subdoel: String?,
    val subdoelen: List<Subdoel>,
    val aanbiedingenGrid: List<AanbodRow>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodRow(
    val aanbodOriginal: String?,
    val aanbod: String?,
    val status: String?
)