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
import java.lang.reflect.ParameterizedType
import org.springframework.security.access.AccessDeniedException

class ValtimoAuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
    private val mappers: List<AuthorizationEntityMapper<*, *>>,
    private val actionProviders: List<ResourceActionProvider<*>>,
    private val permissionRepository: PermissionRepository
): AuthorizationService {
    override fun <T : Any> requirePermission(
        request: EntityAuthorizationRequest<T>
    ) {
        if (!hasPermission(request))
            throw AccessDeniedException("Unauthorized")
    }

    /**
     *   Check for permissions for an (optional) related entity.
     *
     *   @param request the <code>AuthorizationRequest</code> to use when creating new requests
     */
    override fun <T : Any> hasPermission(
        request: AuthorizationRequest<T>
    ) : Boolean {
        return getAuthorizationSpecification(request).isAuthorized();
    }

    override fun <T : Any> getAuthorizationSpecification(
        request: AuthorizationRequest<T>,
        permissions: List<Permission>?
    ): AuthorizationSpecification<T> {
        val usedPermissions = permissions ?: getPermissions(request)

        val factory = (authorizationSpecificationFactories.firstOrNull() {
            it.canCreate(request, usedPermissions)
        } as AuthorizationSpecificationFactory<T>?)?: throw AccessDeniedException("No specification found for given context.")
        return factory.create(request, usedPermissions)
    }

    override fun getPermissions(resourceType: Class<*>, action: Action<*>): List<Permission> {
        return permissionRepository.findAllByResourceTypeAndAction(resourceType, action)
    }

    override fun <FROM, TO> getMapper(
        from: Class<FROM>,
        to: Class<TO>
    ): AuthorizationEntityMapper<FROM, TO> {
        return (mappers.firstOrNull() {
            it.supports(from, to)
        } as AuthorizationEntityMapper<FROM, TO>?)?: throw AccessDeniedException("No entity mapper found for given arguments.")
    }

    override fun <T : Any> getAvailableActionsForResource(clazz: Class<T>): List<Action<T>> {
        return actionProviders
            .filter { (it.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0].equals(clazz) }
            .map { it as ResourceActionProvider<T> }
            .map { it.getAvailableActions() }
            .flatten()
    }

    private fun getPermissions(context: EntityAuthorizationRequest<*>): List<Permission> {
        val userRoles = SecurityUtils.getCurrentUserRoles()
        return permissionRepository.findAllByRoleKeyIn(userRoles)
            .filter { permission ->
                context.resourceType == permission.resourceType && context.action == permission.action
            }
    }
}