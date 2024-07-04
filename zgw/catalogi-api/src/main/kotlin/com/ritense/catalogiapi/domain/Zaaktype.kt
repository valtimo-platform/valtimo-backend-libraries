/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.catalogiapi.domain

import com.ritense.zgw.domain.Vertrouwelijkheid
import java.net.URI
import java.time.LocalDate
import java.time.Period

data class Zaaktype(
    val url: URI,
    val identificatie: String,
    val omschrijving: String,
    val omschrijvingGeneriek: String? = null,
    val vertrouwelijkheidaanduiding: Vertrouwelijkheid,
    val doel: String,
    val aanleiding: String,
    val toelichting: String? = null,
    val indicatieInternOfExtern: String,
    val handelingInitiator: String,
    val onderwerp: String,
    val handelingBehandelaar: String,
    val doorlooptijd: Period? = null,
    val servicenorm: String? = null,
    val opschortingEnAanhoudingMogelijk: Boolean,
    val verlengingMogelijk: Boolean,
    val verlengingstermijn: Period? = null,
    val trefwoorden: List<String>? = null,
    val publicatieIndicatie: Boolean,
    val publicatietekst: String? = null,
    val verantwoordingsrelatie: List<String>? = null,
    val productenOfDiensten: List<URI> = emptyList(),
    val selectielijstProcestype: URI? = null,
    val referentieproces: Referentieproces,
    val catalogus: URI,
    val statustypen: List<String>? = null,
    val resultaattypen: List<String>? = null,
    val eigenschappen: List<String>? = null,
    val informatieobjecttypen: List<String>? = null,
    val roltypen: List<String>? = null,
    val besluittypen: List<String> = emptyList(),
    val deelzaaktypen: List<String>? = null,
    val gerelateerdeZaaktypen: List<GerelateerdeZaaktype> = emptyList(),
    val beginGeldigheid: LocalDate,
    val eindeGeldigheid: LocalDate? = null,
    val versiedatum: LocalDate,
    val concept: Boolean? = null,
)