/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.valtimo.contract.utils.SecurityUtils
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

class IkoClient(
    private val restClientBuilder: RestClient.Builder,
) {
    fun getById(
        baseUrl: URI,
        searchPath: String,
        id: String,
    ): JsonNode {
        try {
            val result = restClientBuilder
                .clone()
                .build()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme(baseUrl.scheme)
                        .host(baseUrl.host)
                        .path(baseUrl.path)
                        .port(baseUrl.port)
                        .pathSegment("searches")
                        .path(searchPath)
                        .pathSegment(id)
                        .build()
                }
                .header(AUTHORIZATION, SecurityUtils.getCurrentJwtTokenValue())
                .retrieve()
                .body<JsonNode>()!!

            return result
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: remove try-catch with mock data
            return jacksonObjectMapper().readTree(
                """{"persoon":{"aNummer": "8940402024","burgerservicenummer": "999993653","datumEersteInschrijvingGBA": { "type": "Datum", "datum": "2013-11-02", "langFormaat": "2 november 2013"},"geslacht": { "code": "V", "omschrijving": "vrouw"},"leeftijd": 39,"naam": { "aanduidingNaamgebruik": { "code": "E", "omschrijving": "eigen geslachtsnaam" }, "voornamen": "Suzanne", "geslachtsnaam": "Moulin", "voorletters": "S.", "volledigeNaam": "Suzanne Moulin"},"nationaliteiten": [ { "type": "Nationaliteit", "datumIngangGeldigheid": { "type": "DatumOnbekend", "onbekend": true, "langFormaat": "onbekend" }, "nationaliteit": { "code": "0057", "omschrijving": "Franse" }, "redenOpname": { "code": "301", "omschrijving": "Vaststelling bezit vreemde nationaliteit" } }],"geboorte": { "land": { "code": "5001", "omschrijving": "Canada" }, "plaats": { "omschrijving": "Thann" }, "datum": { "type": "Datum", "datum": "1985-12-01", "langFormaat": "1 december 1985" }}},"zaken":[{"zaakNummer":"ZAAK_2025-065498051","type":"Beoordelen vernieuwing bijstand","status":"In behandeling","uiterlijkeEinddatum":"11-09-2026"},{"zaakNummer":"ZAAK_2025-065716573","type":"Aanvraag SHV","status":"Afgewezen","uiterlijkeEinddatum":"18-04-2025"},{"zaakNummer":"ZAAK_2024-654165472","type":"Aanvraag Ooievaarspas","status":"Toegekend","uiterlijkeEinddatum":"31-12-2025"},{"zaakNummer":"ZAAK_2024-354166545","type":"Uitstel betaling GBLT","status":"Afgerond","uiterlijkeEinddatum":"05-06-2025"}]}"""
            )
        }
    }

    fun search(
        baseUrl: URI,
        searchPath: String,
        searchType: String? = null,
        filters: Map<String, String>,
    ): JsonNode {
        try {
            val result = restClientBuilder
                .clone()
                .build()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme(baseUrl.scheme)
                        .host(baseUrl.host)
                        .path(baseUrl.path)
                        .port(baseUrl.port)
                        .pathSegment("searches")
                        .path(searchPath)
                        .queryParam("type", searchType)
                        .queryParams(
                            LinkedMultiValueMap(
                                filters
                                    .map { (key, value) -> key to listOf(value) }
                                    .associate { it })
                        )
                        .build()
                }
                .header(AUTHORIZATION, SecurityUtils.getCurrentJwtTokenValue())
                .retrieve()
                .body<JsonNode>()!!

            return result
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: remove try-catch with mock data
            return jacksonObjectMapper().readTree(
                """{"type": "ZoekMetGeslachtsnaamEnGeboortedatum","personen": [{"burgerservicenummer": "999993653","naam": {"voornamen": "Suzanne","geslachtsnaam": "Moulin","voorletters": "S.","volledigeNaam": "Suzanne Moulin","aanduidingNaamgebruik": {"code": "E","omschrijving": "eigen geslachtsnaam"}}}]}"""
            )
        }
    }

}
