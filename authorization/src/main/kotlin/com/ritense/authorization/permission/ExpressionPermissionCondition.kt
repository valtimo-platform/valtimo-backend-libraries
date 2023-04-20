package com.ritense.authorization.permission

import com.jayway.jsonpath.JsonPath
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


class ExpressionPermissionCondition<T: Comparable<T>>(
    val field: String,
    val path: String,
    val operator: PermissionExpressionOperator,
    val value: T,
    val clazz: Class<T>
): PermissionCondition() {
    override val permissionConditionType: PermissionConditionType = PermissionConditionType.EXPRESSION
    override fun <T: Any> isValid(entity: T): Boolean {
        val fieldValue = reflectionFindFieldIfString(entity) ?: return false

        return evaluateExpression(
            JsonPath.read(fieldValue, path)
        )
    }

    override fun <T: Any> toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path: Path<Any>? = createDatabaseObjectPath(field, root, resourceType)

        return operator.toPredicate(
            criteriaBuilder,
            queryDialectHelper.getValueForPath(criteriaBuilder, path, this.path, clazz),
            value
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

    private fun evaluateExpression(pathValue: T): Boolean {
        return operator.evaluate(pathValue, value)
    }
}