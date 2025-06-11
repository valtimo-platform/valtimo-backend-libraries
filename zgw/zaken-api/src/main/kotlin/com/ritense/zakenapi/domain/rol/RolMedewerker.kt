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
 * Data class voor een MEDEWERKER.
 *
 * @property identificatie            Een korte unieke aanduiding van de MEDEWERKER. Maximaal 24 tekens.
 * @property achternaam               De achternaam zoals de MEDEWERKER die in het dagelijkse verkeer gebruikt. Maximaal 200 tekens.
 * @property voorletters              De verzameling letters gevormd door de eerste letter van alle in volgorde voorkomende voornamen. Maximaal 20 tekens.
 * @property voorvoegselAchternaam    Dat deel van de geslachtsnaam dat voorkomt in Tabel 36 (GBA), voorvoegseltabel, en door een spatie van de geslachtsnaam is. Maximaal 10 tekens.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RolMedewerker(
    val identificatie: String,
    val achternaam: String,
    val voorletters: String,
    val voorvoegselAchternaam: String
)

