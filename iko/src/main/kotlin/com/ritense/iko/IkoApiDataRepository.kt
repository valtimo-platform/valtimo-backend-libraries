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

package com.ritense.iko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.iko.client.IkoApiClient
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_URL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class IkoApiDataRepository(
    private val ikoApiClient: IkoApiClient,
    private val objectMapper: ObjectMapper,
) : IkoConnector {

    override fun getType() = "iko"

    override fun getIkoConnectorPropertyFields(): List<PropertyField> =
        listOf(PropertyField(BASE_URL, PROPERTY_FIELD_TYPE_URL))

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(PropertyField(DATA_AGGREGATE_PATH))

    override fun getDataRequestPropertyFields(): List<PropertyField> = listOf(PropertyField(DATA_REQUEST_PATH))

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        val node = objectMapper.readTree(
            """[{"persoon":{"aNummer":"8940402024","burgerservicenummer":"999993653","datumEersteInschrijvingGBA":{"type":"Datum","datum":"2013-11-02","langFormaat":"2 november 2013"},"geslacht":{"code":"V","omschrijving":"vrouw"},"leeftijd":39,"naam":{"aanduidingNaamgebruik":{"code":"E","omschrijving":"eigen geslachtsnaam"},"voornamen":"Suzanne","geslachtsnaam":"Moulin","voorletters":"S.","volledigeNaam":"Suzanne Moulin"},"nationaliteiten":[{"type":"Nationaliteit","datumIngangGeldigheid":{"type":"DatumOnbekend","onbekend":true,"langFormaat":"onbekend"},"nationaliteit":{"code":"0057","omschrijving":"Franse"},"redenOpname":{"code":"301","omschrijving":"Vaststelling bezit vreemde nationaliteit"}}],"geboorte":{"land":{"code":"5001","omschrijving":"Canada"},"plaats":{"omschrijving":"Thann"},"datum":{"type":"Datum","datum":"1985-12-01","langFormaat":"1 december 1985"}},"verblijfplaats":{"type":"Adres","verblijfadres":{"officieleStraatnaam":"Boterdiep","korteStraatnaam":"Boterdiep","huisnummer":31,"postcode":"3077AW","woonplaats":"Rotterdam"},"functieAdres":{"code":"W","omschrijving":"woonadres"},"adresseerbaarObjectIdentificatie":"0599010000208579","nummeraanduidingIdentificatie":"0599200000219678","datumVan":{"type":"Datum","datum":"2015-08-08","langFormaat":"8 augustus 2015"},"datumIngangGeldigheid":{"type":"Datum","datum":"2015-08-08","langFormaat":"8 augustus 2015"}},"immigratie":{"datumVestigingInNederland":{"type":"Datum","datum":"2013-11-02","langFormaat":"2 november 2013"},"landVanwaarIngeschreven":{"code":"5002","omschrijving":"Frankrijk"},"indicatieVestigingVanuitBuitenland":true},"gemeenteVanInschrijving":{"code":"0599","omschrijving":"Rotterdam"},"datumInschrijvingInGemeente":{"type":"Datum","datum":"2013-11-02","langFormaat":"2 november 2013"},"adressering":{"aanhef":"Geachte mevrouw Moulin","aanschrijfwijze":{"naam":"S. Moulin"},"gebruikInLopendeTekst":"mevrouw Moulin","adresregel1":"Boterdiep 31","adresregel2":"3077 AW  ROTTERDAM"},"gezag":[],"kinderen":[],"ouders":[{"geslacht":{"code":"V","omschrijving":"vrouw"},"ouderAanduiding":"1","datumIngangFamilierechtelijkeBetrekking":{"type":"Datum","datum":"1985-12-01","langFormaat":"1 december 1985"},"naam":{"voornamen":"Lisette","geslachtsnaam":"Béamont","voorletters":"L."},"geboorte":{"land":{"code":"5002","omschrijving":"Frankrijk"},"plaats":{"omschrijving":"Clermont-Ferrand"},"datum":{"type":"Datum","datum":"1956-12-01","langFormaat":"1 december 1956"}}},{"geslacht":{"code":"M","omschrijving":"man"},"ouderAanduiding":"2","datumIngangFamilierechtelijkeBetrekking":{"type":"Datum","datum":"1985-12-01","langFormaat":"1 december 1985"},"naam":{"voornamen":"Guîllaumé","geslachtsnaam":"Moulin","voorletters":"G."},"geboorte":{"land":{"code":"5002","omschrijving":"Frankrijk"},"plaats":{"omschrijving":"Toulon"},"datum":{"type":"Datum","datum":"1955-04-03","langFormaat":"3 april 1955"}}}],"partners":[{"geslacht":{"code":"M","omschrijving":"man"},"soortVerbintenis":{"code":"H","omschrijving":"huwelijk"},"naam":{"voornamen":"Jean Marie","geslachtsnaam":"Beaudelaire","voorletters":"J.M."},"geboorte":{"land":{"code":"5002","omschrijving":"Frankrijk"},"plaats":{"omschrijving":"Saint Amarin"},"datum":{"type":"Datum","datum":"1976-08-01","langFormaat":"1 augustus 1976"}},"aangaanHuwelijkPartnerschap":{"datum":{"type":"Datum","datum":"2008-12-01","langFormaat":"1 december 2008"},"land":{"code":"5002","omschrijving":"Frankrijk"},"plaats":{"omschrijving":"Thann"}}}]},"zaken":{"status":400,"title":"Ten minste één parameter moet worden opgegeven.","type":"https://tools.ietf.org/html/rfc7231#section-6.5.1","detail":"Bad request.","instance":"https://api-test.nl/lvbag/individuelebevragingen/v2/adressen?","code":"paramsRequired"}}]"""
        )
        val list = (node as ArrayNode).toList()
        return PageImpl(list)
    }

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>
    ): Page<JsonNode> {
        return findAll(config, filters, Pageable.unpaged())
    }

    companion object {
        private const val BASE_URL = "baseUrl"
        private const val DATA_AGGREGATE_PATH = "dataAggregatePath"
        private const val DATA_REQUEST_PATH = "dataRequestPath"
    }
}