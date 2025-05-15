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

package com.ritense.openzaak.service.impl.model.catalogi

import com.fasterxml.jackson.annotation.JsonInclude
import java.net.URI

data class ResultaatType(
    val url: URI?,
    val zaaktype: URI,
    val omschrijving: String,
    val resultaattypeomschrijving: URI,
    val omschrijvingGeneriek: String? = null,
    val selectielijstklasse: URI,
    val toelichting: String? = null,
    val archiefnominatie: Archiefnominatie? = null,
    val archiefactietermijn: String? = null,
    val brondatumArchiefprocedure: BrondatumArchiefprocedure? = null
) {
    class BrondatumArchiefprocedure(
        val afleidingswijze: Afleidingswijze,
        val datumkenmerk: String? = null,
        val einddatumBekend: Boolean? = null,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        val objecttype: String? = null,
        val registratie: String? = null,
        val procestermijn: String? = null
    )

    enum class Archiefnominatie {
        blijvend_bewaren,
        vernietigen
    }

    enum class Afleidingswijze {
        afgehandeld,
        ander_datumkenmerk,
        eigenschap,
        gerelateerde_zaak,
        hoofdzaak,
        ingangsdatum_besluit,
        termijn,
        vervaldatum_besluit,
        zaakobject
    }

}