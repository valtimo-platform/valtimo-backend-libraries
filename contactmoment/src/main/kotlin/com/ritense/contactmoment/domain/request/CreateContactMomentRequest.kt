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

package com.ritense.contactmoment.domain.request

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateContactMomentRequest(
    val vorigContactmoment: String? = null,
    val bronorganisatie: String,
    val registratiedatum: String? = null,
    val kanaal: String? = null,
    val voorkeurskanaal: String? = null,
    val voorkeurstaal: String? = null,
    val tekst: String? = null,
    val onderwerpLinks: List<String>? = null,
    val initiatiefnemer: String? = null,
    val medewerker: String? = null,
    val medewerkerIdentificatie: MedewerkerIdentificatieRequest? = null,
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class MedewerkerIdentificatieRequest(
        val identificatie: String? = null,
        val achternaam: String? = null,
        val voorletters: String? = null,
        val voorvoegselAchternaam: String? = null,
    )
}