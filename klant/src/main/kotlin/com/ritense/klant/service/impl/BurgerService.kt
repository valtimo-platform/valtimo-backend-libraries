/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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
import com.ritense.klant.domain.SubjectIdentificatie
import com.ritense.klant.service.BurgerService
import kotlin.random.Random

class BurgerService(
    private val openKlantClientProperties: OpenKlantClientProperties,
    private val openKlantClient: OpenKlantClient
) : BurgerService {
    override fun getBurger(bsn: String) = openKlantClient.getKlant(bsn)

    override fun createBurger(bsn: String): Klant {
        val klantRequest = KlantCreationRequest(
            openKlantClientProperties.rsin,
            generateKlantNummer(),
            "http://www.invalid-url.com/", // TODO: retrieve websiteUrl
            "natuurlijk_persoon",
            SubjectIdentificatie(
                bsn
            )
        )

        return openKlantClient.postKlant(klantRequest)
    }

    override fun ensureBurgerExists(bsn: String): Klant {
        var klant = getBurger(bsn)
        if (klant == null) {
            klant = createBurger(bsn)
        }
        return klant
    }

    private fun generateKlantNummer(): String {
        // generate 8 digit random number
        return Random.nextInt(10000000, 99999999).toString()
    }
}