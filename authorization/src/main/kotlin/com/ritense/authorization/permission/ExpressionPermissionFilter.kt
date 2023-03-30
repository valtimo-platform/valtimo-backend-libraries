package com.ritense.authorization.permission

import com.jayway.jsonpath.JsonPath
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


class ExpressionPermissionFilter(
    val field: String,
    val path: String,
    val operator: ExpressionOperator,
    val value: String
): PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.EXPRESSION
    override fun isValid(entity: Any): Boolean {
        val fieldValue = reflectionFindFieldIfString(entity) ?: return false

        return evaluateExpression(
            JsonPath.read(fieldValue, path)
        )
    }

    override fun <T> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path: Path<Any>? = createDatabaseObjectPath(field, root, resourceType)

        return queryDialectHelper
            .getJsonValueExistsInPathExpression(
                criteriaBuilder,
                path,
                this.path,
                this.value
            )
    }

    private fun reflectionFindFieldIfString(entity: Any): String? {
        var currentEntity = entity
        field.split('.').forEach {
            val declaredField = currentEntity.javaClass.getDeclaredField(it)
            declaredField.trySetAccessible()
            currentEntity = declaredField.get(currentEntity)
        }
        return if (currentEntity is String) {
            currentEntity as String
        } else {
            null
        }
    }

    private fun evaluateExpression(pathValue: String): Boolean {
        return operator.evaluate(pathValue, value)
    }
}