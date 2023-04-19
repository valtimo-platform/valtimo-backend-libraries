package com.ritense.authorization.permission

import com.jayway.jsonpath.JsonPath
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


class ExpressionPermissionCondition(
    val field: String,
    val path: String,
    val operator: ExpressionOperator,
    val value: String
): PermissionCondition() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.EXPRESSION
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
        // TODO: Change expressionoperator to handle this, so no conditionals are necessary
        if (ExpressionOperator.LESS_THAN == operator) {
            return queryDialectHelper
                .getJsonValueLessThanExistsInPathExpression(
                    criteriaBuilder,
                    path,
                    this.path,
                    this.value
                )
        }

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

    private fun evaluateExpression(pathValue: Any): Boolean {
        return operator.evaluate(pathValue.toString(), value)
    }
}