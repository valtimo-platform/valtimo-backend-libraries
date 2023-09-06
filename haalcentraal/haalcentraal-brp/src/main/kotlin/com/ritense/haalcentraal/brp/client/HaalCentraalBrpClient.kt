/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.haalcentraal.brp.client

import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpProperties
import com.ritense.haalcentraal.brp.domain.Personen
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class HaalCentraalBrpClient(
    private val webclientBuilder: WebClient.Builder
) {
    suspend fun findPeople(
        geboortedatum: String?,
        geslachtsnaam: String?,
        burgerservicenummer: String?,
        haalcentraalBrpProperties: HaalCentraalBrpProperties
    ): Personen {
        val people = webClient(haalcentraalBrpProperties)
            .get()
            .uri {
                val uriBUilder = it.path("/ingeschrevenpersonen")
                    .queryParam("geboorte__datum", geboortedatum)
                    .queryParam("naam__geslachtsnaam", geslachtsnaam)
                    .queryParam("burgerservicenummer", burgerservicenummer)
                    .queryParam("fields", "burgerservicenummer,naam,geboorte.datum")
                uriBUilder.build()
            }
            .retrieve()
            .awaitBody<Personen>()

        if (!burgerservicenummer.isNullOrEmpty()
            &&  people.embedded.ingeschrevenpersonen != null && people.embedded.ingeschrevenpersonen.size > 1 ) {
           throw IllegalStateException("Multiple people found for BSN: $burgerservicenummer")
        }

        return people
    }

    private fun webClient(haalCentraalBrpProperties: HaalCentraalBrpProperties): WebClient {
        return webclientBuilder
            .clone()
            .baseUrl(haalCentraalBrpProperties.url!!)
            .defaultHeader("X-API-KEY", haalCentraalBrpProperties.apiKey!!)
            .build()
    }
}