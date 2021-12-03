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

package com.ritense.connector.service

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.domain.ConnectorTypeId
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.repository.ConnectorTypeRepository
import com.ritense.connector.web.rest.request.CreateConnectorInstanceRequest
import com.ritense.connector.web.rest.request.ModifyConnectorInstanceRequest
import com.ritense.connector.web.rest.result.CreateConnectorInstanceResult
import com.ritense.connector.web.rest.result.CreateConnectorInstanceResultFailed
import com.ritense.connector.web.rest.result.CreateConnectorInstanceResultSucceeded
import com.ritense.connector.web.rest.result.ModifyConnectorInstanceResult
import com.ritense.connector.web.rest.result.ModifyConnectorInstanceResultFailed
import com.ritense.connector.web.rest.result.ModifyConnectorInstanceResultSucceeded
import com.ritense.valtimo.contract.result.OperationError
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.interceptor.TransactionAspectSupport
import java.util.UUID
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

@Transactional
open class ConnectorService(
    private val context: ApplicationContext,
    private val connectorTypeInstanceRepository: ConnectorTypeInstanceRepository,
    private val connectorTypeRepository: ConnectorTypeRepository
) {

    open fun getConnectorTypes(): List<ConnectorType> {
        return connectorTypeRepository.findAll()
    }

    open fun getConnectorInstances(pageable: Pageable = Pageable.unpaged()): Page<ConnectorInstance> {
        return connectorTypeInstanceRepository.findAll(pageable)
    }

    open fun getConnectorInstancesByType(typeId: UUID, pageable: Pageable = Pageable.unpaged()): Page<ConnectorInstance> {
        return connectorTypeInstanceRepository.findAllByTypeId(ConnectorTypeId.existingId(typeId), pageable)
    }

    open fun getConnectorInstanceById(id: UUID): ConnectorInstance {
        return connectorTypeInstanceRepository.getById(ConnectorInstanceId.existingId(id))
    }

    open fun createConnectorInstance(
        typeId: UUID,
        name: String,
        connectorProperties: ConnectorProperties
    ): CreateConnectorInstanceResult {
        return createConnectorInstance(
            CreateConnectorInstanceRequest(typeId, name, connectorProperties)
        )
    }

    open fun createConnectorInstance(
        createConnectorInstanceRequest: CreateConnectorInstanceRequest
    ): CreateConnectorInstanceResult {
        return try {
            require(
                !connectorTypeInstanceRepository.existsConnectorTypeInstanceByName(createConnectorInstanceRequest.name)
            ) { "connectorTypeInstance already exists under same name" }
            val connectorType = connectorTypeRepository.findById(ConnectorTypeId.existingId(createConnectorInstanceRequest.typeId))
            if (connectorType.isPresent && !connectorType.get().allowMultipleConnectorInstances) {
                // Check if the same connectorType has been used before
                require(
                    !connectorTypeInstanceRepository.existsConnectorTypeInstanceByType(connectorType.get())
                ) { "Only one '${connectorType.get().name}' connector is allowed" }
            }
            val connectorInstance = connectorTypeInstanceRepository.save(
                ConnectorInstance(
                    ConnectorInstanceId.newId(UUID.randomUUID()),
                    connectorType.orElseThrow(),
                    createConnectorInstanceRequest.name,
                    createConnectorInstanceRequest.connectorProperties
                )
            )
            val connector = load(connectorInstance)
            connector.onCreate(connectorInstance)
            return CreateConnectorInstanceResultSucceeded(connectorInstance)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            CreateConnectorInstanceResultFailed(errors)
        } catch (ex: RuntimeException) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            CreateConnectorInstanceResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    open fun modifyConnectorTypeInstance(
        modifyConnectorInstanceRequest: ModifyConnectorInstanceRequest
    ): ModifyConnectorInstanceResult {
        return try {
            val connectorTypeInstance = connectorTypeInstanceRepository.getById(
                ConnectorInstanceId.existingId(modifyConnectorInstanceRequest.id)
            )
            val connectorType = connectorTypeRepository.findById(ConnectorTypeId.existingId(modifyConnectorInstanceRequest.typeId))
            connectorTypeInstance.changeType(connectorType.orElseThrow())
            connectorTypeInstance.changeName(modifyConnectorInstanceRequest.name)
            connectorTypeInstance.changeProperties(modifyConnectorInstanceRequest.connectorProperties)
            val connector = load(connectorTypeInstance)
            connectorTypeInstanceRepository.save(connectorTypeInstance)
            connector.onEdit(connectorTypeInstance)
            return ModifyConnectorInstanceResultSucceeded(connectorTypeInstance)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            ModifyConnectorInstanceResultFailed(errors)
        } catch (ex: RuntimeException) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            ModifyConnectorInstanceResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    open fun removeConnectorTypeInstance(id: UUID) {
        val connectorInstance = getConnectorInstanceById(id)
        val connector = load(connectorInstance)
        connectorTypeInstanceRepository.deleteById(connectorInstance.id)
        connector.onDelete(connectorInstance)
    }

    /**
     * Instantiates a connector by name with configured properties.
     * Get bean will retrieve the bean from the context. Connector beans should be annotated
     * with <code>@Scope(BeanDefinition.SCOPE_PROTOTYPE)</code> to ensure call based creation.
     *
     * @param name the name of the connector instance
     */
    open fun load(name: String): Connector {
        val connectorTypeInstance = connectorTypeInstanceRepository.findByName(name)
        requireNotNull(connectorTypeInstance) { "ConnectorTypeInstance was not found with name: ${name}" }
        return load(connectorTypeInstance)
    }

    /**
     * Instantiates a connector by ConnectorInstance.
     * Get bean will retrieve the bean from the context. Connector beans should be annotated
     * with <code>@Scope(BeanDefinition.SCOPE_PROTOTYPE)</code> to ensure call based creation.
     *
     * @param connectorInstance the connector instance entity representing the connector
     */
    open fun load(connectorInstance: ConnectorInstance): Connector {
        val connector = context.getBean(connectorInstance.type.className) as Connector
        requireNotNull(connector) { "Connector bean was not found with name: ${connectorInstance.type.className}" }
        with(connector) {
            setProperties(connectorInstance.connectorProperties)
        }
        return connector
    }
}