/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.authorization.specification.AuthorizationSpecification

interface AuthorizationService {
    fun <T : Any> requirePermission(
        request: AuthorizationRequest<T>
    )

    fun <T : Any> getAuthorizedRoles(request: AuthorizationRequest<T>): Set<Role>

    fun <T : Any> getAuthorizationSpecification(
        request: AuthorizationRequest<T>,
        permissions: List<Permission>? = null
    ): AuthorizationSpecification<T>

    fun getPermissions(resourceType: Class<*>, action: Action<*>): List<Permission>

    fun <FROM, TO> getMapper(from: Class<FROM>, to: Class<TO>): AuthorizationEntityMapper<FROM, TO>

    fun <T : Any> getAvailableActionsForResource(clazz: Class<T>): List<Action<T>>

    fun <T : Any> hasPermission(request: AuthorizationRequest<T>): Boolean
}