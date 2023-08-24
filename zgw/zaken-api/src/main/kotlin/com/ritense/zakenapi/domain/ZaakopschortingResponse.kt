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

package com.ritense.zakenapi.domain

data class ZaakopschortingResponse(
    val url: String,
    val uuid: String,
    val identificatie: String,
    val bronorganisatie: String,
    val omschrijving: String,
    val toelichting: String,
    val zaaktype: String,
    val registratiedatum: String,
    val verantwoordelijkeOrganisatie: String,
    val startdatum: String,
    val einddatum: String?,
    val einddatumGepland: String?,
    val uiterlijkeEinddatumAfdoening: String?,
    val publicatiedatum: String?,
    val communicatiekanaal: String,
    val productenOfDiensten: List<String>,
    val vertrouwelijkheidaanduiding: String,
    val betalingsindicatie: String,
    val betalingsindicatieWeergave: String,
    val laatsteBetaaldatum: String?,
    val zaakgeometrie: String?,
    val verlenging: Verlenging?,
    val opschorting: Opschorting?,
    val selectielijstklasse: String,
    val hoofdzaak: String?,
    val deelzaken: List<String>,
    val relevanteAndereZaken: List<String>,
    val eigenschappen: List<String>,
    val status: String?,
    val kenmerken: List<String>,
    val archiefnominatie: String?,
    val archiefstatus: String,
    val archiefactiedatum: String?,
    val resultaat: String?,
    val opdrachtgevendeOrganisatie: String
) {
    data class Verlenging(
        val reden: String,
        val duur: String
    )

    data class Opschorting(
        val indicatie: Boolean,
        val reden: String
    )
}

