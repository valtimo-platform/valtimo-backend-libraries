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

package com.ritense.authorization

import com.ritense.authorization.permission.Permission
import com.ritense.valtimo.contract.utils.SecurityUtils

class ValtimoAuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
    private val mappers: List<AuthorizationEntityMapper<*, *>>,
    private val permissionRepository: PermissionRepository
): AuthorizationService {
    override fun <T : Any> requirePermission(context: AuthorizationRequest<T>, entity: T, permissions: List<Permission>?) {

        if (!getAuthorizationSpecification(context, permissions).isAuthorized(entity))
            throw RuntimeException("Unauthorized")
    }

    override fun <T : Any> getAuthorizationSpecification(
        context: AuthorizationRequest<T>,
        permissions: List<Permission>?
    ): AuthorizationSpecification<T> {
        val usedPermissions = permissions ?: getPermissions()

        return if (AuthorizationContext.ignoreAuthorization) {
            NoopAuthorizationSpecification(usedPermissions, context)
        } else {
            (authorizationSpecificationFactories.first {
                it.canCreate(context)
            } as AuthorizationSpecificationFactory<T>).create(context, usedPermissions)
        }
    }

    override fun <FROM, TO> getMapper(from: Class<FROM>, to: Class<TO>): AuthorizationEntityMapper<FROM, TO> {
        return mappers.first {
            it.supports(from, to)
        } as AuthorizationEntityMapper<FROM, TO>
    }

    private fun getPermissions(): List<Permission> {
        return permissionRepository.findAllByRoleKeyIn(SecurityUtils.getCurrentUserRoles())
    }
}