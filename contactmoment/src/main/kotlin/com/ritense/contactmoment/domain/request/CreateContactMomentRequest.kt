/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

data class CreateContactMomentRequest(
    val vorigContactmoment: String?,
    val bronorganisatie: String,
    val registratiedatum: String?,
    val kanaal: String?,
    val voorkeurskanaal: String?,
    val voorkeurstaal: String?,
    val tekst: String?,
    val onderwerpLinks: List<String>?,
    val initiatiefnemer: String?,
    val medewerker: String?,
    val medewerkerIdentificatie: MedewerkerIdentificatieRequest?,
) {
    data class MedewerkerIdentificatieRequest(
        val identificatie: String?,
        val achternaam: String?,
        val voorletters: String?,
        val voorvoegselAchternaam: String?,
    )
}