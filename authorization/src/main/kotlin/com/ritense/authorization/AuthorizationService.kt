package com.ritense.authorization

import com.ritense.authorization.permission.Permission

class AuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>,
    private val mappers: List<AuthorizationEntityMapper<*, *>>,
    private val defaultPermissions: List<Permission>
) {
    fun <T : Any> requirePermission(context: AuthorizationRequest<T>, entity: T, permissions: List<Permission>?) {

        if (!(getAuthorizationSpecification(context, permissions).isAuthorized(entity)))
            throw RuntimeException("Unauthorized")
    }

    fun <T : Any> getAuthorizationSpecification(
        context: AuthorizationRequest<T>,
        permissions: List<Permission>?
    ): AuthorizationSpecification<T> {
        return (authorizationSpecificationFactories.first {
            it.canCreate(context)
        } as AuthorizationSpecificationFactory<T>).create(context, permissions ?: defaultPermissions)
    }

    fun <FROM, TO> getMapper(from: Class<FROM>, to: Class<TO>): AuthorizationEntityMapper<FROM, TO> {
        return mappers.first {
            it.appliesTo(from, to)
        } as AuthorizationEntityMapper<FROM, TO>
    }
}