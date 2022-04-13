/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.haalcentraal.brp.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.haalcentraal.brp.client.HaalCentraalBRPClient
import com.ritense.haalcentraal.brp.domain.Person
import com.ritense.haalcentraal.brp.domain.Personen
import com.ritense.haalcentraal.brp.web.rest.request.GetPeopleRequest
import kotlinx.coroutines.runBlocking
import java.security.InvalidParameterException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ConnectorType(name = "HaalCentraal")
class HaalCentraalBRPConnector(
    private var haalCentraalBRPProperties: HaalCentraalBRPProperties,
    private var haalCentraalBRPClient: HaalCentraalBRPClient
) : Connector {


    fun findPeople(request: GetPeopleRequest): List<Person> {
        validateRequest(request)
        val personen: Personen = runBlocking {
            haalCentraalBRPClient.findPeople(
                request.geboortedatum,
                request.geslachtsnaam,
                request.bsn,
                haalCentraalBRPProperties
            )
        }

        return personen.embedded.ingeschrevenpersonen?.map {
            Person(
                it.burgerservicenummer,
                it.naam?.voornamen,
                it.naam?.voorletters,
                it.naam?.geslachtsnaam,
                toIsoLocalDate(
                    it.geboorte?.datum?.jaar,
                    it.geboorte?.datum?.maand,
                    it.geboorte?.datum?.dag),
            )
        } ?: emptyList()
    }

    override fun getProperties(): HaalCentraalBRPProperties {
        return haalCentraalBRPProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        haalCentraalBRPProperties = connectorProperties as HaalCentraalBRPProperties
    }

    private fun toIsoLocalDate(year: Int?, month: Int?, day: Int?): String? {
        return if (year == null || month == null || day == null) {
            null
        } else {
            LocalDate.of(year, month, day).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    private fun validateRequest(request: GetPeopleRequest) {
        if (request.bsn?.isNotEmpty() == true) {
            return
        }

        if ((request.geslachtsnaam == null && request.geboortedatum?.isNotEmpty() == true)
            || request.geslachtsnaam?.isNotEmpty() == true && request.geboortedatum == null) {
            throw InvalidParameterException("When not searching with a bsn the name and birthdate must both be filled in")
        }
    }
}