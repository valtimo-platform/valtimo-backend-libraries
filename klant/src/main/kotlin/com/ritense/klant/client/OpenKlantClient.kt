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

package com.ritense.klant.client

import com.ritense.klant.domain.klanten.Klant

class OpenKlantClient(
    private val openKlantClientConfig: OpenKlantClientConfig,
    private val openKlantTokenGenerator: OpenKlantTokenGenerator
) {
    fun getKlant(bsn: String): Klant {
        val token = openKlantTokenGenerator.generateTokenForBsn(
            openKlantClientConfig.secret,
            openKlantClientConfig.clientId,
            bsn
        )
        val klantPage = RequestBuilder
            .builder()
            .get()
            .baseUrl(openKlantClientConfig.url)
            .path("/klanten/api/v1/klanten")
            .queryParam("subjectNatuurlijkPersoon__inpBsn", bsn)
            .queryParam("page", 1)
            .token(token)
            .executePaged(Klant::class.java)
        if (klantPage.results.size > 1) {
            throw IllegalStateException("Too many klanten found for bsn $bsn")
        }

        return klantPage.results.firstOrNull()
            ?: throw IllegalStateException("No klant found for bsn $bsn!")
    }
}