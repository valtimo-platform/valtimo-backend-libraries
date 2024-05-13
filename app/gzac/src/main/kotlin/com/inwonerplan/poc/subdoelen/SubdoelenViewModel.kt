package com.inwonerplan.poc.subdoelen

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.api.StamtabellenApi
import com.inwonerplan.model.Aandachtspunt
import com.inwonerplan.model.Subdoel
import com.ritense.formviewmodel.viewmodel.ViewModel

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubdoelenViewModel(
    val subdoelenGrid: List<SubdoelenGridRow>,
    val aandachtspunten: List<Aandachtspunt>
) : ViewModel {
    override fun update(): ViewModel {
        println("Updating")
        return this.copy(
            subdoelenGrid = subdoelenGrid.map {
                it.copy(
                    aandachtspuntOriginal = it.aandachtspunt,
                    subdoelen = getSubdoelenForAandachtspunten(it.aandachtspunt)
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

data class SubdoelenGridRow(
    val aandachtspuntOriginal: String,
    val aandachtspunt: String,
    val subdoelen: List<Subdoel>,
    val subdoel: String? = null,
    val status: String? = null
)