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
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.authorization.specification.AuthorizationSpecificationFactory
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.utils.SecurityUtils
import java.lang.reflect.ParameterizedType
import mu.KotlinLogging
import org.springframework.security.access.AccessDeniedException

class ValtimoAuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
    private val mappers: List<AuthorizationEntityMapper<*, *>>,
    private val actionProviders: List<ResourceActionProvider<*>>,
    private val permissionRepository: PermissionRepository,
    private val userManagementService: UserManagementService
) : AuthorizationService {
    override fun <T : Any> requirePermission(
        request: EntityAuthorizationRequest<T>
    ) {
        if (!hasPermission(request)) {
            if (request.action.key != Action.DENY) {
                logger.debug { "Unauthorized. User is missing permission '${request.action.key}' on '${request.resourceType}'." }
            }
            throw AccessDeniedException("Unauthorized")
        }
    }

    override fun <T : Any> getAuthorizedRoles(request: EntityAuthorizationRequest<T>): Set<Role> {
        return getPermissions(request.resourceType, request.action)
            .groupBy { it.role }
            .filter { getAuthorizationSpecification(request, it.value, enablePermissionLogging = false).isAuthorized() }
            .map { it.key }
            .toSet()
    }

    /**
     *   Check for permissions for an (optional) related entity.
     *
     *   @param request the <code>AuthorizationRequest</code> to use when creating new requests
     */
    override fun <T : Any> hasPermission(
        request: AuthorizationRequest<T>
    ): Boolean {
        return getAuthorizationSpecification(request).isAuthorized()
    }

    override fun <T : Any> getAuthorizationSpecification(
        request: AuthorizationRequest<T>,
        permissions: List<Permission>?
    ): AuthorizationSpecification<T> {
        val usedPermissions = permissions ?: getPermissions(request)

        return getAuthorizationSpecification(request, usedPermissions, enablePermissionLogging = true)
    }

    override fun getPermissions(resourceType: Class<*>, action: Action<*>): List<Permission> {
        return permissionRepository.findAllByResourceTypeAndAction(resourceType, action)
    }

    override fun <FROM, TO> getMapper(
        from: Class<FROM>,
        to: Class<TO>
    ): AuthorizationEntityMapper<FROM, TO> {
        return (mappers.firstOrNull {
            it.supports(from, to)
        } as AuthorizationEntityMapper<FROM, TO>?)
            ?: throw AccessDeniedException("No entity mapper found for given arguments.")
    }

    override fun <T : Any> getAvailableActionsForResource(clazz: Class<T>): List<Action<T>> {
        return actionProviders
            .filter { (it.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0].equals(clazz) }
            .map { it as ResourceActionProvider<T> }
            .map { it.getAvailableActions() }
            .flatten()
    }

    private fun <T : Any> getAuthorizationSpecification(
        request: AuthorizationRequest<T>,
        permissions: List<Permission>,
        enablePermissionLogging:Boolean
    ): AuthorizationSpecification<T> {
        if (enablePermissionLogging) {
            logPermissions(request, permissions)
        }

        val factory = (authorizationSpecificationFactories.firstOrNull {
            it.canCreate(request, permissions)
        } as AuthorizationSpecificationFactory<T>?)
            ?: throw AccessDeniedException("No specification found for given context.")
        return factory.create(request, permissions)
    }

    private fun getPermissions(context: AuthorizationRequest<*>): List<Permission> {
        val userRoles = if (context.user == null || context.user == SecurityUtils.getCurrentUserLogin()) {
            SecurityUtils.getCurrentUserRoles()
        } else {
            userManagementService.findByEmail(context.user).orElse(null)
                ?.roles
                ?: return emptyList()
        }
        return permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(userRoles)
            .filter { permission ->
                context.resourceType == permission.resourceType && context.action == permission.action
            }
    }

    private fun logPermissions(request: AuthorizationRequest<*>, permissions: List<Permission>) {
        if (request.action.key == Action.DENY) {
            logger.error {
                "Access denied on '${request.resourceType}'. This generally indicates attempting to " +
                    "access a resource without considering authorization. Please refer to the Valtimo documentation."
            }
        } else {
            val permissionsLogLine = permissions.joinToString(", ") { "${it.id}:${it.role.key}" }
            val logLine =
                "Requesting permissions '${request.action.key}:${request.resourceType.simpleName}' for user '${request.user}' and found matching permissions: [$permissionsLogLine]"
            logger.debug { logLine }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}