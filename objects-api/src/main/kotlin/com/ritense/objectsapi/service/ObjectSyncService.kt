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

package com.ritense.objectsapi.service

import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.objectsapi.domain.sync.ObjectSyncConfig
import com.ritense.objectsapi.domain.sync.ObjectSyncConfigId
import com.ritense.objectsapi.repository.ObjectSyncConfigRepository
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.request.ModifyObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.result.CreateObjectSyncConfigResult
import com.ritense.objectsapi.web.rest.result.CreateObjectSyncConfigResultFailed
import com.ritense.objectsapi.web.rest.result.CreateObjectSyncConfigResultSucceeded
import com.ritense.objectsapi.web.rest.result.ModifyObjectSyncConfigResult
import com.ritense.objectsapi.web.rest.result.ModifyObjectSyncConfigResultFailed
import com.ritense.objectsapi.web.rest.result.ModifyObjectSyncConfigResultSucceeded
import com.ritense.valtimo.contract.result.OperationError
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID
import javax.validation.ConstraintViolationException

open class ObjectSyncService(
    private val objectSyncConfigRepository: ObjectSyncConfigRepository,
    private val connectorTypeInstanceRepository: ConnectorTypeInstanceRepository
) {

    fun getObjectSyncConfig(id: UUID): ObjectSyncConfig? {
        return objectSyncConfigRepository.getById(ObjectSyncConfigId.existingId(id))
    }

    fun getObjectSyncConfig(documentDefinitionName: String, pageable: Pageable = Pageable.unpaged()): Page<ObjectSyncConfig> {
        return objectSyncConfigRepository.findAllByDocumentDefinitionName(documentDefinitionName, pageable)
    }

    fun getObjectSyncConfig(pageable: Pageable = Pageable.unpaged()): Page<ObjectSyncConfig> {
        return objectSyncConfigRepository.findAll(pageable)
    }

    fun createObjectSyncConfig(request: CreateObjectSyncConfigRequest): CreateObjectSyncConfigResult {
        return try {
            require(
                connectorTypeInstanceRepository.findById(ConnectorInstanceId.existingId(request.connectorInstanceId)).isPresent
            ) { "connectorTypeInstance with $request.connectorInstanceId not found" }
            val objectSyncConfig = objectSyncConfigRepository.save(
                ObjectSyncConfig(
                    ObjectSyncConfigId.newId(UUID.randomUUID()),
                    request.connectorInstanceId,
                    request.enabled,
                    request.documentDefinitionName,
                    request.objectTypeId
                )
            )
            return CreateObjectSyncConfigResultSucceeded(objectSyncConfig)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            CreateObjectSyncConfigResultFailed(errors)
        } catch (ex: RuntimeException) {
            CreateObjectSyncConfigResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    fun modifyObjectSyncConfig(request: ModifyObjectSyncConfigRequest): ModifyObjectSyncConfigResult {
        return try {
            val objectSyncConfig = objectSyncConfigRepository.save(
                ObjectSyncConfig(
                    ObjectSyncConfigId.existingId(request.id),
                    request.connectorInstanceId,
                    request.enabled,
                    request.documentDefinitionName,
                    request.objectTypeId
                )
            )
            return ModifyObjectSyncConfigResultSucceeded(objectSyncConfig)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            ModifyObjectSyncConfigResultFailed(errors)
        } catch (ex: RuntimeException) {
            ModifyObjectSyncConfigResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    fun removeObjectSyncConfig(id: UUID) {
        objectSyncConfigRepository.deleteById(ObjectSyncConfigId.existingId(id))
    }
}