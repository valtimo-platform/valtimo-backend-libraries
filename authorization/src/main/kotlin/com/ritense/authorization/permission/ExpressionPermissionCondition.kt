package com.ritense.authorization.permission

import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.ritense.authorization.jackson.ComparableDeserializer
import com.ritense.authorization.permission.ExpressionPermissionCondition.Companion.EXPRESSION
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


@JsonTypeName(EXPRESSION)
data class ExpressionPermissionCondition<T: Comparable<T>>(
    val field: String,
    val path: String,
    val operator: PermissionExpressionOperator,
    @JsonDeserialize(using = ComparableDeserializer::class)
    val value: T,
    val clazz: Class<T>
): PermissionCondition(PermissionConditionType.EXPRESSION) {
    override fun <T: Any> isValid(entity: T): Boolean {
        val fieldValue = findEntityJsonField(entity) ?: return false

        return try {
            evaluateExpression(
                JsonPath.read(fieldValue, path)
            )
        } catch (e: PathNotFoundException) {
            false
        }

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

    private fun findEntityJsonField(entity: Any): String? {
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

    companion object {
        const val EXPRESSION = "expression"
    }
}