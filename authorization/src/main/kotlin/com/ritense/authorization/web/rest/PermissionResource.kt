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

import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.web.rest.request.PermissionAvailableRequest
import com.ritense.authorization.web.rest.result.PermissionAvailableResult
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class PermissionResource(
    private var authorizationService: AuthorizationService
) {

    @PostMapping("/v1/permission")
    fun getPluginDefinitions(@RequestBody permissionsPresentRequest: List<PermissionAvailableRequest>)
        : ResponseEntity<List<PermissionAvailableResult>> {
        // TODO: For each element, map each resource to the actual resource type
        // TODO: For each element, for each context element, map to the actual entity
        // TODO: Check if there is a permission that would grant access to that entity given the resource and action
        // TODO: Catch exception and take this response as a false, otherwise return a true
        // TODO: Collect results and return as response entity

        return ResponseEntity.ok(listOf())
    }

    private fun hasPermission(permissionsPresentRequest: PermissionAvailableRequest) {
        permissionsPresentRequest.action
    }
}
