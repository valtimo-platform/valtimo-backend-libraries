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
