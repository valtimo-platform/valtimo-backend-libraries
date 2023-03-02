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

package com.ritense.objectsapi.web.rest.impl

import com.ritense.objectsapi.web.rest.ObjectSyncConfigResource as IObjectSyncConfigResource
import com.ritense.objectsapi.domain.sync.ObjectSyncConfig
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.request.ModifyObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.result.CreateObjectSyncConfigResult
import com.ritense.objectsapi.web.rest.result.ModifyObjectSyncConfigResult
import org.springframework.http.ResponseEntity
import java.util.UUID

class ObjectSyncConfigResource(
    private val objectSyncService: ObjectSyncService
) : IObjectSyncConfigResource {

    override fun getConfig(id: UUID): ResponseEntity<ObjectSyncConfig> {
        return ResponseEntity.ok(objectSyncService.getObjectSyncConfig(id))
    }

    override fun getConfigs(documentDefinitionName: String): ResponseEntity<List<ObjectSyncConfig>> {
        return ResponseEntity.ok(
            objectSyncService.getObjectSyncConfig(documentDefinitionName).toList()
        )
    }

    override fun create(request: CreateObjectSyncConfigRequest): ResponseEntity<CreateObjectSyncConfigResult> {
        val result = objectSyncService.createObjectSyncConfig(request)
        return when (result.objectSyncConfig()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ResponseEntity.ok(result)
        }
    }

    override fun modify(request: ModifyObjectSyncConfigRequest): ResponseEntity<ModifyObjectSyncConfigResult> {
        val result = objectSyncService.modifyObjectSyncConfig(request)
        return when (result.objectSyncConfig()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ResponseEntity.ok(result)
        }
    }

    override fun remove(id: UUID): ResponseEntity<Void> {
        objectSyncService.removeObjectSyncConfig(id)
        return ResponseEntity.noContent().build()
    }
}