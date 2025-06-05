package com.ritense.zakenapi.domain.rol

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Data class voor een organisatorische eenheid.
 *
 * @property identificatie   Een korte identificatie van de organisatorische eenheid. Maximaal 24 tekens.
 * @property naam            De feitelijke naam van de organisatorische eenheid. Maximaal 50 tekens.
 * @property isGehuisvestIn  De aanduiding waar deze eenheid gehuisvest is. Maximaal 24 tekens.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RolOrganisatorischeEenheid(
    val identificatie: String,
    val naam: String,
    val isGehuisvestIn: String
) : BetrokkeneIdentificatie()
