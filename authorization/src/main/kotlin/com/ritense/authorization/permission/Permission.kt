package com.ritense.authorization.permission

import com.ritense.authorization.Action
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

class Permission(
    val resourceType: Class<*>,
    val action: Action,
    val filters: List<PermissionFilter>
) {
    fun appliesTo(entity: Any): Boolean {
        return if (entity::class.java == resourceType) {
            filters
                .map { it.isValid(entity) }
                .all { it }
        } else {
            false
        }
    }

    fun toPredicate(
        Root<*> root,
        CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder
    ) {

    }
}