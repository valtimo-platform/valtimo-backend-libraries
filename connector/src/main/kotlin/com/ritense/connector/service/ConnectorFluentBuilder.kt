/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.connector.service

import com.ritense.connector.domain.Connector

/**
 *  Provides fluent way of connector loading.
 *
 *      ${connectorFluentBuilder
 *          .builder()
 *          .withConnector("aConnectorInstanceName")
 *          .aMethodInAConnectorClassInstance()
 *    }
 *
 */
class ConnectorFluentBuilder(
    private val connectorService: ConnectorService
) {

    fun builder(): Builder {
        return Builder(connectorService)
    }

    data class Builder(
        val connectorService: ConnectorService
    ) {
        /**
         * Load and return a connector by name.
         *
         * @param name the name of the connector instance
         */
        fun withConnector(name: String): Connector {
            return connectorService.loadByName(name)
        }
    }
}