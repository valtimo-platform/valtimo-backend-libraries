package com.ritense.authorization.permission

import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

abstract class PermissionCondition(
    val permissionConditionType: PermissionConditionType
) {
    abstract fun <T: Any> isValid(entity: T): Boolean
    abstract fun <T: Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate

    fun <T> createDatabaseObjectPath(field: String, root: Root<T>, resourceType: Class<T>): Path<Any>? {
        //TODO: resourceType is unused, is it really needed?

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