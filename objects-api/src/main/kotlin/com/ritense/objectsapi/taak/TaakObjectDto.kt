package com.ritense.objectsapi.taak

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class TaakObjectDto(
    val bsn: String?,
    val kvk: String?,
    @JsonProperty("verwerker_taak_id") val verwerkerTaakId: UUID,
    @JsonProperty("formulier_id") val formulierId: String,
    val data: Map<String, Any>?,
    @JsonProperty("verzonden_data") val verzondenData: Map<String, Any>?,
    val status: TaakObjectStatus = TaakObjectStatus.open
) {
    init {
        require(kvk != null || bsn != null) { "BSN and/or KvK number is required!" }
    }
}

enum class TaakObjectStatus {
    open, ingediend, verwerkt, gesloten
}