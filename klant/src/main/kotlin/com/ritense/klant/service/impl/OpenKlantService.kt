package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.domain.KlantSearchFilter
import kotlin.random.Random

open class OpenKlantService(
    private val openKlantClient: OpenKlantClient
) {
    protected fun generateKlantNummer(): String {
        var klantnummerValid = false
        var klantnummer = ""
        while (!klantnummerValid) {
            klantnummer = Random.nextInt(10000000, 99999999).toString()
            klantnummerValid = validateKlantnummerNotTaken(klantnummer)
        }
        return klantnummer
    }

    protected fun getDefaultWebsiteUrl(): String {
        return "http://example.org"
    }

    private fun validateKlantnummerNotTaken(klantnummer: String): Boolean {
        val klantPage = openKlantClient.searchKlanten(
            KlantSearchFilter(
                klantnummer = klantnummer
            )
        )
        return klantPage.results.size == 0
    }
}