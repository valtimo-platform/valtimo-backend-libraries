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

package com.ritense.haalcentraal.brp.web.rest

import com.ritense.connector.service.ConnectorService
import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpConnector
import com.ritense.haalcentraal.brp.domain.Person
import com.ritense.haalcentraal.brp.web.rest.request.GetPeopleRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api"])
class HaalCentraalBrpResource(
    val connectorService: ConnectorService
) {

    @PostMapping("/v1/haalcentraal/personen")
    fun findPersonByBsn(
        @RequestBody request: GetPeopleRequest
    ): ResponseEntity<List<Person>> {
        val connector = connectorService.loadByClassName(HaalCentraalBrpConnector::class.java)
        return ResponseEntity.ok(connector.findPeople(request))
    }


}