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

package com.ritense.objectsapi.web.rest

import com.ritense.objectsapi.domain.sync.ObjectSyncConfig
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.request.ModifyObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.result.CreateObjectSyncConfigResult
import com.ritense.objectsapi.web.rest.result.ModifyObjectSyncConfigResult
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid


@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
interface ObjectSyncConfigResource {

    @GetMapping("/v1/object/sync/config/{id}")
    fun getConfig(@PathVariable(name = "id") id: UUID): ResponseEntity<ObjectSyncConfig>

    @GetMapping("/v1/object/sync/config")
    fun getConfigs(
        @RequestParam("documentDefinitionName") documentDefinitionName: String
    ): ResponseEntity<List<ObjectSyncConfig>>

    @PostMapping("/v1/object/sync/config")
    fun create(
        @Valid @RequestBody request: CreateObjectSyncConfigRequest
    ): ResponseEntity<CreateObjectSyncConfigResult>

    @PutMapping("/v1/object/sync/config")
    fun modify(
        @Valid @RequestBody request: ModifyObjectSyncConfigRequest
    ): ResponseEntity<ModifyObjectSyncConfigResult>

    @DeleteMapping("/v1/object/sync/config/{id}")
    fun remove(@PathVariable(name = "id") id: UUID): ResponseEntity<Void>
}
