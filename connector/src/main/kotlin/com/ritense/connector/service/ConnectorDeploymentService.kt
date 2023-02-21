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

package com.ritense.connector.service

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.domain.ConnectorTypeId
import com.ritense.connector.repository.ConnectorTypeRepository
import mu.KotlinLogging
import java.util.UUID

class ConnectorDeploymentService(
    private val connectorTypeRepository: ConnectorTypeRepository
) {

    fun deployAll(connectors: List<Connector>): List<ConnectorType> {
        val connectorTypes = mutableListOf<ConnectorType>()
        connectors.forEach {
            val connectorTypeAnnotation = it.javaClass.getAnnotation(com.ritense.connector.domain.meta.ConnectorType::class.java)
            val name = connectorTypeAnnotation.name
            val allowMultipleConnectors = connectorTypeAnnotation.allowMultipleConnectors
            var connectorType = connectorTypeRepository.findByName(name)
            val simpleClassName = ConnectorType.getNameFromClass(it.javaClass)
            if (connectorType == null) {
                logger.info { "creating new connectorType $name" }
                connectorType = ConnectorType(
                    id = ConnectorTypeId.newId(UUID.randomUUID()),
                    name = name,
                    className = simpleClassName,
                    connectorProperties = it.getProperties(),
                    allowMultipleConnectorInstances = allowMultipleConnectors
                )
            } else {
                logger.info { "connectorType already deployed updating existing $name" }
                connectorType.name = name
                connectorType.className = simpleClassName
                connectorType.connectorProperties = it.getProperties()
                connectorType.allowMultipleConnectorInstances = allowMultipleConnectors
            }
            connectorTypeRepository.save(connectorType)
            connectorTypes.add(connectorType)
        }
        return connectorTypes
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}