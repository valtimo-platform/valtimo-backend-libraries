package com.ritense.authorization

class AuthorizationService(
    private val authorizationSpecificationFactories: List<AuthorizationSpecificationFactory<*>>
) {
    fun <T> requirePermission(context: AuthorizationRequest<T>) {
        if (!(getAuthorizationSpecification(context).isAuthorized(context)))
            throw RuntimeException("Unauthorized")
    }

    fun <T> getAuthorizationSpecification(context: AuthorizationRequest<T>): AuthorizationSpecification<T> {
        return (authorizationSpecificationFactories.first {
            it.canCreate(context)
        } as AuthorizationSpecificationFactory<T>).create(context)
    }
}