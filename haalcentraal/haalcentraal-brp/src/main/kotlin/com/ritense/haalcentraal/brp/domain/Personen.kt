/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.haalcentraal.brp.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class Personen(
    @JsonProperty("_embedded")
    val embedded: Embedded
) {
    data class Embedded(
        val ingeschrevenpersonen: List<Persoonsgegevens>?
    )

    data class Persoonsgegevens(
        val burgerservicenummer: String,
        val naam: PersoonNaam?,
        val geboorte: PersoonGeboorte?,
    )

   data class PersoonNaam(
        val voornamen: String?,
        val voorletters: String?,
        val geslachtsnaam: String?
    )

   data class PersoonGeboorte(
        val datum: PersoonGeboorteDatum?
   )

   data class PersoonGeboorteDatum (
       val jaar: Int?,
       val maand: Int?,
       val dag: Int?
   )
}

