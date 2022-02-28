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

package com.ritense.besluit.domain.request

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateBesluitRequest(
    val identificatie: String? = null,
    val verantwoordelijkeOrganisatie: String? = null,
    val besluittype: String? = null,
    val zaak: String? = null,
    val datum: String? = null,
    val toelichting: String? = null,
    val bestuursorgaan: String? = null,
    val ingangsdatum: String? = null,
    val vervaldatum: String? = null,
    val vervalreden: String? = null,
    val publicatiedatum: String? = null,
    val verzendddatum: String? = null,
    val uiterlijkeReactiedatum: String? = null
)