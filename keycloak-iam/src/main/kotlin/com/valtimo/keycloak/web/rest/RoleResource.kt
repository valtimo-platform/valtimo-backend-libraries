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

package com.valtimo.keycloak.web.rest

import com.valtimo.keycloak.service.RoleService
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.ws.rs.NotFoundException

@RestController
@RequestMapping(value = ["/api/v1/role"])
class RoleResource(
    private val roleService: RoleService
) {

    @GetMapping
    fun getRoles(
        @RequestParam roleNamePrefix: String?
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(roleService.findRoles(roleNamePrefix))
        } catch (e: NotFoundException) {
            logger.debug("Could not find roles: ${e.message}")
            ResponseEntity.notFound().build()
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}