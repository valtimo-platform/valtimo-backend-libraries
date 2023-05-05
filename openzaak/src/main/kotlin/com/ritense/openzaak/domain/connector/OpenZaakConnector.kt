/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.openzaak.domain.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType

@ConnectorType(name = "OpenZaak", allowMultipleConnectors = false)
class OpenZaakConnector(
    private var openZaakProperties: OpenZaakProperties
): Connector {

    override fun getProperties(): OpenZaakProperties {
        return openZaakProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        openZaakProperties = connectorProperties as OpenZaakProperties
    }

    fun getName(): String {
        return "OpenZaak"
    }
}
