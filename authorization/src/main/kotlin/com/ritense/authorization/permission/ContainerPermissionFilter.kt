package com.ritense.authorization.permission

import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class ContainerPermissionFilter(
    val entity: String,
    val filters: List<PermissionFilter>
): PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.CONTAINER
    override fun isValid(entity: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T> toPredicate(root: Root<T>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder, resourceType: Class<T>, queryDialectHelper: QueryDialectHelper): Predicate {
        TODO("Not yet implemented")
    }
}