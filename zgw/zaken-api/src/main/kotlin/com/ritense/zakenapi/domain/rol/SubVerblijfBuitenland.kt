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
