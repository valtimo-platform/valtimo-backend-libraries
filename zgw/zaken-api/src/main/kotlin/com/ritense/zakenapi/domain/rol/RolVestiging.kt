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
