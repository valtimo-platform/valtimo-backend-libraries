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
 * Data class voor onderverblijf in het buitenland.
 *
 * @property lndLandcode            De landcode conform BRP Land/Gebied-tabel. Maximaal 4 tekens. (verplicht)
 * @property lndLandnaam            De landnaam conform BRP Land/Gebied-tabel. Maximaal 40 tekens. (verplicht)
 * @property subAdresBuitenland_1   Eerste regel van adres in het buitenland. Maximaal 35 tekens. (optioneel)
 * @property subAdresBuitenland_2   Tweede regel van adres in het buitenland. Maximaal 35 tekens. (optioneel)
 * @property subAdresBuitenland_3   Derde regel van adres in het buitenland. Maximaal 35 tekens. (optioneel)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubVerblijfBuitenland(
    val lndLandcode: String,
    val lndLandnaam: String,
    val subAdresBuitenland_1: String? = null,
    val subAdresBuitenland_2: String? = null,
    val subAdresBuitenland_3: String? = null
)
