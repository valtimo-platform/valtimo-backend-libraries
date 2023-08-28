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

package com.ritense.contactmoment.client

import com.ritense.contactmoment.connector.ContactMomentProperties
import com.ritense.contactmoment.domain.ContactMoment
import com.ritense.contactmoment.domain.ResultPage
import com.ritense.contactmoment.domain.request.CreateContactMomentRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

open class ContactMomentClient(
    private val webclientBuilder: WebClient.Builder,
    private val contactMomentTokenGenerator: ContactMomentTokenGenerator,
) {

    var contactMomentProperties: ContactMomentProperties? = null

    /**
     * Get a list of ContactMomenten
     */
    suspend fun getContactMomenten(page: Int): ResultPage<ContactMoment> {
        return webClient()
            .get()
            .uri {
                it.path("/contactmomenten/api/v1/contactmomenten")
                    .queryParam("ordering", "-registratiedatum")
                    .queryParam("page", page).build()
            }
            .retrieve()
            .awaitBody()
    }

    /**
     * Create a ContactMoment
     *
     * @param request the <code>CreateContactMomentRequest</code> to use when creating new requests
     */
    suspend fun createContactMoment(request: CreateContactMomentRequest): ContactMoment {
        return webClient()
            .post()
            .uri("/contactmomenten/api/v1/contactmomenten")
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }

    private fun webClient(): WebClient {
        val token = contactMomentTokenGenerator.generateToken(
            contactMomentProperties!!.secret,
            contactMomentProperties!!.clientId
        )

        return webclientBuilder
            .clone()
            .baseUrl(contactMomentProperties!!.url)
            .defaultHeader("Authorization", "Bearer $token")
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .build()
    }
}