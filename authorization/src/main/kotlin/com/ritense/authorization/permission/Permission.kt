package com.ritense.authorization.permission

import com.ritense.authorization.Action
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class Permission(
    val resourceType: Class<*>,
    val action: Action,
    val conditions: List<PermissionCondition>
) {
    fun <T> appliesTo(resourceType: Class<T>, entity: Any?): Boolean {
        return if (this.resourceType == resourceType) {
            if (entity == null && conditions.isNotEmpty()) {
                return false
            }
            conditions
                .map { it.isValid(entity!!) }
                .all { it }
        } else {
            false
        }
    }

    fun <T: Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        return criteriaBuilder
            .and(
                *conditions.map {
                    it.toPredicate(
                        root,
                        query,
                        criteriaBuilder,
                        resourceType,
                        queryDialectHelper)
                }.toTypedArray()
            )
    }
}