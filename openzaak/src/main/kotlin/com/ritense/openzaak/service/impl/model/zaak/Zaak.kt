/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service.impl.model.zaak

import java.net.URI
import java.util.UUID

data class Zaak(
    val url: URI,
    val uuid: UUID,
    val identificatie: String,
    val bronorganisatie: String,
    val omschrijving: String,
    val toelichting: String,
    val zaaktype: URI,
    val registratiedatum: String,
    val verantwoordelijkeOrganisatie: String,
    val startdatum: String,
    val einddatum: String?,
    val einddatumGepland: String?,
    val uiterlijkeEinddatumAfdoening: String?,
    val publicatiedatum: String?,
    val communicatiekanaal: String,
    val productenOfDiensten: Collection<URI>?,
    val vertrouwelijkheidaanduiding: String,
    val betalingsindicatie: String,
    val betalingsindicatieWeergave: String,
    val laatsteBetaaldatum: String?,
    val zaakgeometrie: Zaakgeometrie?,
    val verlenging: Verlenging?,
    val opschorting: Opschorting?,
    val selectielijstklasse: URI,
    val hoofdzaak: URI?,
    val deelzaken: Collection<URI>?,
    val relevanteAndereZaken: Collection<RelevanteAndereZaken>?,
    val eigenschappen: Collection<URI>?,
    val status: URI?,
    var statusOmschrijving: String?,
    val kenmerken: Collection<Kenmerken>?,
    val archiefnominatie: String?,
    val archiefstatus: String,
    val archiefactiedatum: String?,
    val resultaat: URI?,
    var resulaatOmschrijving: String?
) {
    class Zaakgeometrie(
        val type: String?,
        val coordinates: Collection<Int>?
    )

    class Verlenging(
        val reden: String?,
        val duur: String?
    )

    class Opschorting(
        val indicatie: Boolean?,
        val reden: String?
    )

    class RelevanteAndereZaken(
        val url: URI,
        val aardRelatie: String
    )

    class Kenmerken(
        val kenmerk: String,
        val bron: String
    )
}