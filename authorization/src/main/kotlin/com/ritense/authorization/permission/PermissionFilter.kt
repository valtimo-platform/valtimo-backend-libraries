package com.ritense.authorization.permission

import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

abstract class PermissionFilter {
    abstract val permissionFilterType: PermissionFilterType
    abstract fun isValid(entity: Any): Boolean
    abstract fun <T> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate

    fun <T> createDatabaseObjectPath(field: String, root: Root<T>, resourceType: Class<T>): Path<Any>? {
        var path: Path<Any>? = null
        field.split('.').forEach {
            path = if (path == null) {
                root.get(it)
            } else {
                path!!.get(it)
            }
        }

        return path
    }
}