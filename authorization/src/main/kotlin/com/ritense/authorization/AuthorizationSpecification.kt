package com.ritense.authorization

import com.ritense.authorization.permission.Permission
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate

abstract class AuthorizationSpecification<T: Any> (
    protected val permissions: List<Permission>,
    protected val authContext: AuthorizationRequest<T>
): Specification<T> {
    fun isAuthorized(entity: T): Boolean {
        return permissions.filter { permission ->
            entity::class.java == permission.resourceType && authContext.action == permission.action
        }.any { permission ->
            permission.appliesTo(authContext.resourceType, entity)
        }
    }

    fun combinePredicates(criteriaBuilder: CriteriaBuilder, predicates: List<Predicate>): Predicate {
        return criteriaBuilder.and(criteriaBuilder.or(*predicates.toTypedArray()))
    }
}