package com.ritense.authorization

import org.springframework.data.jpa.domain.Specification

interface AuthorizationSpecification<T>: Specification<T> {
    fun isAuthorized(authContext : AuthorizationRequest<T>, entity: T): Boolean
}