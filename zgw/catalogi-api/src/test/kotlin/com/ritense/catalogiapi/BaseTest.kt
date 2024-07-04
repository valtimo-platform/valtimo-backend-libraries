/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.catalogiapi

import com.ritense.catalogiapi.domain.GerelateerdeZaaktype
import com.ritense.catalogiapi.domain.Referentieproces
import com.ritense.catalogiapi.domain.Zaaktype
import com.ritense.zgw.domain.Vertrouwelijkheid
import java.net.URI
import java.time.LocalDate
import java.time.Period

abstract class BaseTest {

    protected fun newZaaktype(
        url: URI,
        omschrijving: String,
        omschrijvingGeneriek: String = "omschrijvingGeneriek"
    ): Zaaktype = Zaaktype(
        url = url,
        identificatie = omschrijving,
        omschrijving = omschrijving,
        omschrijvingGeneriek = omschrijvingGeneriek,
        vertrouwelijkheidaanduiding = Vertrouwelijkheid.ZAAKVERTROUWELIJK,
        doel = "doel",
        aanleiding = "aanleiding",
        indicatieInternOfExtern = "indicatieInternOfExtern",
        handelingInitiator = "handelingInitiator",
        onderwerp = "onderwerp",
        handelingBehandelaar = "handelingBehandelaar",
        doorlooptijd = Period.ofDays(84),
        opschortingEnAanhoudingMogelijk = false,
        verlengingMogelijk = false,
        trefwoorden = listOf("trefwoorden"),
        publicatieIndicatie = false,
        verantwoordingsrelatie = listOf("verantwoordingsrelatie"),
        productenOfDiensten = listOf(URI("ritense.com/productenOfDiensten")),
        referentieproces = Referentieproces("referentieproces"),
        catalogus = URI("ritense.com/catalogus"),
        roltypen = listOf("roltypen"),
        besluittypen = listOf("besluittypen"),
        deelzaaktypen = listOf("deelzaaktypen"),
        gerelateerdeZaaktypen = listOf(GerelateerdeZaaktype(URI("ritense.com/gerelateerdeZaaktypen"), "aardRelatie")),
        beginGeldigheid = LocalDate.now(),
        versiedatum = LocalDate.now(),
    )
}