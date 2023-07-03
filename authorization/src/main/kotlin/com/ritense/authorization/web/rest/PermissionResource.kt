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

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.RelatedEntityAuthorizationRequest
import com.ritense.authorization.web.rest.request.PermissionAvailableRequest
import com.ritense.authorization.web.rest.result.PermissionAvailableResult
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class PermissionResource(
    private var authorizationService: AuthorizationService
) {

    @PostMapping("/v1/permissions")
    fun userHasPermission(@RequestBody permissionsPresentRequest: List<PermissionAvailableRequest>)
        : ResponseEntity<List<PermissionAvailableResult>> {

        val permissionResponse: List<PermissionAvailableResult>

        try {
            permissionResponse = permissionsPresentRequest.map {
                PermissionAvailableResult(
                    it.resource,
                    it.action,
                    it.context,
                    authorizationService.hasPermission(
                        RelatedEntityAuthorizationRequest(
                            it.getResourceAsClass(),
                            Action(it.action),
                            it.context.getResourceAsClass(),
                            it.context.identifier
                        )
                    )
                )
            }
        } catch (cnfe: ClassNotFoundException) {
            throw AccessDeniedException("Unauthorized")
        }

        return ResponseEntity.ok(permissionResponse)
    }
}
