/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.klant.service.impl

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.client.OpenKlantClientProperties
import com.ritense.klant.domain.Klant
import com.ritense.klant.domain.KlantCreationRequest
import com.ritense.klant.domain.NietNatuurlijkPersoonSubjectIdentificatie
import com.ritense.klant.service.BedrijfService

class BedrijfService(
    private val openKlantClientProperties: OpenKlantClientProperties,
    private val openKlantClient: OpenKlantClient
) : OpenKlantService(openKlantClient), BedrijfService {
    override fun getBedrijf(kvk: String) = openKlantClient.getKlant(kvk = kvk)

    override fun createBedrijf(kvk: String): Klant {
        val klantRequest = KlantCreationRequest(
            openKlantClientProperties.rsin,
            generateKlantNummer(),
            getDefaultWebsiteUrl(),
            "niet_natuurlijk_persoon",
            NietNatuurlijkPersoonSubjectIdentificatie(
                kvk
            )
        )

        return openKlantClient.postKlant(klantRequest)
    }

    override fun ensureBedrijfExists(kvk: String): Klant {
        var klant = getBedrijf(kvk)
        if (klant == null) {
            klant = createBedrijf(kvk)
        }
        return klant
    }
}