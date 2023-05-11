package com.ritense.authorization

import com.ritense.authorization.permission.Permission

interface AuthorizationService {
    fun <T : Any> requirePermission(context: AuthorizationRequest<T>, entity: T, permissions: List<Permission>?)

    fun <T : Any> getAuthorizationSpecification(
        context: AuthorizationRequest<T>,
        permissions: List<Permission>?
    ): AuthorizationSpecification<T>

    fun <FROM, TO> getMapper(from: Class<FROM>, to: Class<TO>): AuthorizationEntityMapper<FROM, TO>
}