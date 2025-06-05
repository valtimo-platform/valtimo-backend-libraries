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

