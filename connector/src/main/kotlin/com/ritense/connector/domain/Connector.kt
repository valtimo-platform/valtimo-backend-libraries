/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.connector.domain

@Deprecated("Since 12.0.0")
interface Connector {

    @Deprecated("Since 12.0.0")
    fun getProperties(): ConnectorProperties

    @Deprecated("Since 12.0.0")
    fun setProperties(connectorProperties: ConnectorProperties)

    @Deprecated("Since 12.0.0")
    fun onCreate(connectorInstance: ConnectorInstance) {
        //Not implemented by default.
    }

    @Deprecated("Since 12.0.0")
    fun onEdit(connectorInstance: ConnectorInstance) {
        //Not implemented by default.
    }

    @Deprecated("Since 12.0.0")
    fun onDelete(connectorInstance: ConnectorInstance) {
        //Not implemented by default.
    }
}