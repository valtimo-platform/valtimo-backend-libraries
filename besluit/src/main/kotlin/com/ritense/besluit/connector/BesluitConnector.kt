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

package com.ritense.besluit.connector

import com.ritense.besluit.client.BesluitClient
import com.ritense.besluit.domain.Besluit
import com.ritense.besluit.domain.request.CreateBesluitRequest
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ConnectorType(name = "Besluiten")
class BesluitConnector(
    private var besluitProperties: BesluitProperties,
    private var besluitClient: BesluitClient
) : Connector {

    /**
     * Create a Besluit
     */
    fun createBsluit(): Besluit {
        val request = CreateBesluitRequest(
            besluitTypeUrl = "test",
            zaakUrl = "test",
            datum = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            ingangsdatum = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )

        return runBlocking { besluitClient.createBesluit(request) }
    }

    override fun getProperties(): ConnectorProperties {
        return besluitProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        besluitProperties = connectorProperties as BesluitProperties
    }

    companion object {
        const val rootUrlApiVersion = "/api/v1"
    }
}