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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ritense.zgw.Rsin
import com.ritense.zgw.domain.ArchiefStatus
import com.ritense.zgw.domain.Archiefnominatie
import com.ritense.zgw.domain.Vertrouwelijkheid
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZaakResponse(
    val url: URI,
    val uuid: UUID,
    val identificatie: String? = null,
    val bronorganisatie: Rsin,
    val omschrijving: String? = null,
    val toelichting: String? = null,
    val zaaktype: URI,
    val registratiedatum: LocalDate? = null,
    val verantwoordelijkeOrganisatie: Rsin,
    val startdatum: LocalDate,
    val einddatum: LocalDate? = null,
    val einddatumGepland: LocalDate? = null,
    val uiterlijkeEinddatumAfdoening: LocalDate? = null,
    val publicatiedatum: LocalDate? = null,
    val communicatiekanaal: URI? = null,
    val productenOfDiensten: List<URI>? = null,
    val vertrouwelijkheidaanduiding: Vertrouwelijkheid? = null,
    val betalingsindicatie: Betalingsindicatie? = null,
    val betalingsindicatieWeergave: String? = null,
    val laatsteBetaaldatum: LocalDate? = null,
    val zaakgeometrie: Geometry? = null,
    val verlenging: Verlenging? = null,
    val opschorting: Opschorting? = null,
    val selectielijstklasse: URI? = null,
    val hoofdzaak: URI? = null,
    val deelzaken: List<URI>? = null,
    val relevanteAndereZaken: List<RelevanteZaak>? = null,
    val eigenschappen: List<URI>? = null,
    val rollen: List<URI?>? = null,
    val status: String? = null,
    val zaakinformatieobjecten: List<URI>? = null,
    val zaakobjecten: List<URI>? = null,
    val kenmerken: List<Kenmerk>? = null,
    val archiefnominatie: Archiefnominatie? = null,
    val archiefstatus: ArchiefStatus? = null,
    val archiefactiedatum: LocalDate? = null,
    val resultaat: URI? = null,
    val opdrachtgevendeOrganisatie: String? = null,
    val processobjectaard: String? = null,
    val resultaattoelichting: String? = null,
    val startdatumBewaartermijn: LocalDate? = null
)
