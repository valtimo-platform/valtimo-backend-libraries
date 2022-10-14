/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.connector.impl

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import mu.KotlinLogging

@ConnectorType(name = "test-connector")
open class ObjectApiConnectorType(
    private var properties: ConnectorProperties,
    val dummyDependency: DummyDependency
) : Connector {

    init {
        logger.info { "ctor ObjectApiConnectorType with prop: ${properties}" }
    }

    open fun action() {
        logger.info { "called action" }
    }

    override fun getProperties(): ConnectorProperties {
        return properties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        this.properties = connectorProperties
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
