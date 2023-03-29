package com.ritense.authorization

import com.ritense.authorization.permission.Permission

interface AuthorizationSpecificationFactory<T> {

    fun create(context: AuthorizationRequest<T>, permissions: List<Permission>): AuthorizationSpecification<T>

    // Change this to something more dynamic in the future
    fun canCreate(context: AuthorizationRequest<*>): Boolean
}