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

package com.ritense.connector.repository

import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.domain.ConnectorTypeId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ConnectorTypeInstanceRepository : JpaRepository<ConnectorInstance, ConnectorInstanceId> {
    fun existsConnectorTypeInstanceByName(name: String): Boolean
    fun existsConnectorTypeInstanceByType(type: ConnectorType): Boolean
    fun findByName(name: String): ConnectorInstance?
    fun findAllByTypeId(typeId: ConnectorTypeId, pageable: Pageable): Page<ConnectorInstance>
    fun findByType(type: ConnectorType): ConnectorInstance
}