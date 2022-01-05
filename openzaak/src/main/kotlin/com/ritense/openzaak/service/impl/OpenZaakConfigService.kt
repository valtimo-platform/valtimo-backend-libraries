/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service.impl

import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.repository.ConnectorTypeRepository
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.domain.connector.OpenZaakConnector
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import com.ritense.openzaak.service.OpenZaakConfigService

class OpenZaakConfigService(
    private val connectorTypeInstanceRepository: ConnectorTypeInstanceRepository,
    private val connectorTypeRepository: ConnectorTypeRepository,
    private val openZaakProperties: OpenZaakProperties
) : OpenZaakConfigService {

    override fun getOpenZaakConfig(): OpenZaakConfig? {
        val connectorType = connectorTypeRepository.findByName(OpenZaakConnector(openZaakProperties).getName())
            ?: return null
        val connectorInstance = connectorTypeInstanceRepository.findByType(connectorType!!)

        return (connectorInstance.connectorProperties as OpenZaakProperties).openZaakConfig
    }

    override fun hasOpenZaakConfig(): Boolean {
        return when (getOpenZaakConfig()) {
            null -> false
            else -> true
        }
    }

//    override fun testConnection(openZaakConfig: OpenZaakConfig) {
//        try {
//            OpenZaakRequestBuilder(restTemplate, this, tokenGeneratorService)
//                .config(openZaakConfig)
//                .path("catalogi/api/v1/zaaktypen")
//                .build()
//                .execute(String::class.java)
//        } catch (ex: Exception) {
//            throw IllegalStateException("Testing connection failed")
//        }
//    }
}