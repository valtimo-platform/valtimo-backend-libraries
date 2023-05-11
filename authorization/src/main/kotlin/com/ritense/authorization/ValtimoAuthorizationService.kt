package com.ritense.authorization

import com.ritense.authorization.permission.Permission
import com.ritense.valtimo.contract.utils.SecurityUtils

class ValtimoAuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
    private val mappers: List<AuthorizationEntityMapper<*, *>>,
    private val permissionRepository: PermissionRepository
): AuthorizationService {
    override fun <T : Any> requirePermission(context: AuthorizationRequest<T>, entity: T, permissions: List<Permission>?) {

        if (!(getAuthorizationSpecification(context, permissions).isAuthorized(entity)))
            throw RuntimeException("Unauthorized")
    }

    override fun <T : Any> getAuthorizationSpecification(
        context: AuthorizationRequest<T>,
        permissions: List<Permission>?
    ): AuthorizationSpecification<T> {
        return (authorizationSpecificationFactories.first {
            it.canCreate(context)
        } as AuthorizationSpecificationFactory<T>).create(context, permissions ?: getPermissions())
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