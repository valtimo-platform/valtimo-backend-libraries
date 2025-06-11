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

package com.ritense.zakenapi.domain.rol

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Data class representing een Vestiging met bijbehorende gegevens.
 *
 * @property vestigingsNummer      Een korte unieke aanduiding van de Vestiging. Maximaal 24 tekens.
 * @property handelsnaam            De naam van de vestiging waaronder gehandeld wordt. Elke entry maximaal 625 tekens.
 * @property verblijfsadres         Het verblijfsadres van de vestiging, of null als niet van toepassing.
 * @property subVerblijfBuitenland  Details over onderverblijf in het buitenland, of null als niet van toepassing.
 * @property kvkNummer              Een uniek nummer gekoppeld aan de onderneming. Maximaal 8 tekens.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RolVestiging(
    val vestigingsNummer: String? = null,
    val handelsnaam: List<String>? = null,
    val verblijfsadres: Verblijfsadres? = null,
    val subVerblijfBuitenland: SubVerblijfBuitenland? = null,
    val kvkNummer: String? = null
) : BetrokkeneIdentificatie()
