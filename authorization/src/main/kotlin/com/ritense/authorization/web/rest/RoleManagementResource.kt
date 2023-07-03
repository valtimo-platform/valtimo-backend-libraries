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

package com.ritense.authorization.web.rest

import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import com.ritense.authorization.web.rest.request.SaveRoleRequest
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/management", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class RoleManagementResource(
    val roleRepository: RoleRepository
) {
    @GetMapping("/v1/roles")
    fun getPluginDefinitions()
        : ResponseEntity<List<Role>> {
        return ResponseEntity.ok(roleRepository.findAll())
    }

    @PostMapping("/v1/roles")
    fun savePluginDefinition(@RequestBody saveRoleRequest: SaveRoleRequest)
        : ResponseEntity<Role> {
        try {
            val role: Role = roleRepository.save(saveRoleRequest.toRole())
            return ResponseEntity.ok(role)
        } catch (ex: Exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}