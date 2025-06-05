package com.ritense.zakenapi.domain.rol

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Data class voor verblijfsadres van een Vestiging.
 *
 * @property aoaIdentificatie         De unieke identificatie van het OBJECT. Maximaal 100 tekens. (verplicht)
 * @property wplWoonplaatsNaam        De woonplaatsnaam. Maximaal 80 tekens. (verplicht)
 * @property gorOpenbareRuimteNaam    De naam van de openbare ruimte zoals toegekend door de gemeente. Maximaal 80 tekens. (verplicht)
 * @property aoaPostcode              De postcode. Maximaal 7 tekens. (optioneel)
 * @property aoaHuisnummer            Het huisnummer. Integer tussen 0 en 99999. (verplicht)
 * @property aoaHuisletter            De huisletter. Maximaal 1 teken. (optioneel)
 * @property aoaHuisnummertoevoeging  De huisnummertoevoeging. Maximaal 4 tekens. (optioneel)
 * @property inpLocatiebeschrijving   Een omschrijving van de locatie. Maximaal 1000 tekens. (optioneel)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Verblijfsadres(
    val aoaIdentificatie: String,
    val wplWoonplaatsNaam: String,
    val gorOpenbareRuimteNaam: String,
    val aoaPostcode: String? = null,
    val aoaHuisnummer: Int,
    val aoaHuisletter: String? = null,
    val aoaHuisnummertoevoeging: String? = null,
    val inpLocatiebeschrijving: String? = null
)
