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

package com.ritense.besluitenapi.client

import com.fasterxml.jackson.annotation.JsonFormat
import java.net.URI
import java.time.LocalDate

class Besluit(
    val url: URI?,
    val identificatie: String?,
    val verantwoordelijkeOrganisatie: String,
    val besluittype: URI,
    val zaak: URI?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val datum: LocalDate,
    val toelichting: String?,
    val bestuursorgaan: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val ingangsdatum: LocalDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val vervaldatum: LocalDate?,
    val vervalreden: Vervalreden?,
    val vervalredenWeergave: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val publicatiedatum: LocalDate?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val verzenddatum: LocalDate?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val uiterlijkeReactiedatum: LocalDate?,
)