package com.ritense.authorization

import com.ritense.authorization.permission.Permission
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate

abstract class AuthorizationSpecification<T: Any> (
    val permissions: List<Permission>,
    val authContext: AuthorizationRequest<T>
): Specification<T> {
    fun isAuthorized(entity: T): Boolean {
        return permissions.filter {
            entity::class.java == it.resourceType && authContext.action == it.action
        }.any {
            it.appliesTo(authContext.resourceType, entity)
        }
    }

    fun combinePredicates(criteriaBuilder: CriteriaBuilder, predicates: List<Predicate>): Predicate {
        return criteriaBuilder.and(*predicates.toTypedArray())
    }
}