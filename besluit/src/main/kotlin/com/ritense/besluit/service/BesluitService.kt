package com.ritense.besluit.service

import com.ritense.besluit.domain.BesluitType
import com.ritense.openzaak.besluit.BesluitClient

open class BesluitService(
    val besluitClient: BesluitClient
) {

    fun getBesluittypen(): List<BesluitType> {
        return besluitClient.getBesluittypen().results
            .map { BesluitType(it.url, it.omschrijving) }
    }
}