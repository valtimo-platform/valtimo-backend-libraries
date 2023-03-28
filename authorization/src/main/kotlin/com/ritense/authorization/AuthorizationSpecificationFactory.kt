package com.ritense.authorization

interface AuthorizationSpecificationFactory<T> {

    fun create(context: AuthorizationRequest<T>): AuthorizationSpecification<T>

    // Change this to something more dynamic in the future
    fun canCreate(context: AuthorizationRequest<*>): Boolean
}