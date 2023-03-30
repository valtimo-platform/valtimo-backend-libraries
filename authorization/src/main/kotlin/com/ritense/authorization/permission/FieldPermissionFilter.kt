package com.ritense.authorization.permission

import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class FieldPermissionFilter(
    val field: String,
    val value: String
) : PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.FIELD
    override fun isValid(entity: Any): Boolean {
        return reflectionFindField(entity).toString() == value
    }

    override fun <T> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path: Path<Any>? = createDatabaseObjectPath(field, root, resourceType)

        return criteriaBuilder.equal(path, this.value)
    }

    private fun reflectionFindField(entity: Any): Any {
        var currentEntity = entity
        field.split('.').forEach {
                val declaredField = currentEntity.javaClass.getDeclaredField(it)
                declaredField.trySetAccessible()
                currentEntity = declaredField.get(currentEntity)
        }
        return currentEntity
    }
}