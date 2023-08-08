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

import com.fasterxml.jackson.annotation.JsonView
import com.ritense.authorization.PermissionRepository
import com.ritense.authorization.deployment.PermissionDto
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionView
import com.ritense.authorization.web.rest.request.SearchPermissionsRequest
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/management", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class PermissionManagementResource(
    val permissionRepository: PermissionRepository
) {
    @PostMapping("/v1/permissions/search")
    fun searchPermissions(@RequestBody searchRequest: SearchPermissionsRequest): ResponseEntity<List<PermissionDto>> {
        val rolePermissions = permissionRepository
            .findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(searchRequest.roles)
            .map { PermissionDto(it.resourceType, it.action.key, it.conditionContainer.conditions, it.role.key) }
        return ResponseEntity.ok(rolePermissions)
    }

}