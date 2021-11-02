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

package com.ritense.connector.web.rest.impl

import com.ritense.connector.web.rest.ConnectorResource as IConnectorResource
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.connector.web.rest.request.CreateConnectorInstanceRequest
import com.ritense.connector.web.rest.request.ModifyConnectorInstanceRequest
import com.ritense.connector.web.rest.result.CreateConnectorInstanceResult
import com.ritense.connector.web.rest.result.ModifyConnectorInstanceResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.util.UUID

class ConnectorResource(
    private val connectorService: ConnectorService
) : IConnectorResource {

    override fun getTypes(): ResponseEntity<List<ConnectorType>> {
        return ResponseEntity.ok(connectorService.getConnectorTypes())
    }

    override fun getConnectorInstance(instanceId: UUID): ResponseEntity<ConnectorInstance> {
        return ResponseEntity.ok(connectorService.getConnectorInstanceById(instanceId))
    }

    override fun getInstances(pageable: Pageable): ResponseEntity<Page<ConnectorInstance>> {
        return ResponseEntity.ok(connectorService.getConnectorInstances(pageable))
    }

    override fun getInstancesByType(typeId: UUID, pageable: Pageable): ResponseEntity<Page<ConnectorInstance>> {
        return ResponseEntity.ok(connectorService.getConnectorInstancesByType(typeId, pageable))
    }

    override fun create(request: CreateConnectorInstanceRequest): ResponseEntity<CreateConnectorInstanceResult> {
        val result = connectorService.createConnectorInstance(request)
        return when (result.connectorTypeInstance()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ResponseEntity.ok(result)
        }
    }

    override fun modify(request: ModifyConnectorInstanceRequest): ResponseEntity<ModifyConnectorInstanceResult> {
        val result = connectorService.modifyConnectorTypeInstance(request)
        return when (result.connectorTypeInstance()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ResponseEntity.ok(result)
        }
    }

    override fun remove(instanceId: UUID): ResponseEntity<Void> {
        connectorService.removeConnectorTypeInstance(instanceId)
        return ResponseEntity.noContent().build()
    }
}