package com.ritense.authorization.permission

import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.ritense.authorization.jackson.ComparableDeserializer
import com.ritense.authorization.permission.ExpressionPermissionCondition.Companion.EXPRESSION
import com.ritense.valtimo.contract.database.QueryDialectHelper
import java.util.Objects
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


@JsonTypeName(EXPRESSION)
data class ExpressionPermissionCondition<V: Comparable<V>>(
    val field: String,
    val path: String,
    val operator: PermissionExpressionOperator,
    @JsonDeserialize(using = ComparableDeserializer::class)
    val value: V?,
    val clazz: Class<V>
): ReflectingPermissionCondition(PermissionConditionType.EXPRESSION) {
    override fun <E: Any> isValid(entity: E): Boolean {
        val jsonValue = findEntityFieldValue(entity, field)
        if (jsonValue !is String) {
            return value == null && jsonValue == null
        }

        val pathValue = try {
            JsonPath.read<V?>(jsonValue, path)
        } catch (e: PathNotFoundException) {
            null
        }

        return evaluateExpression(
            pathValue
        )

    }

    override fun <E: Any> toPredicate(
        root: Root<E>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<E>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path: Path<Any>? = createDatabaseObjectPath(field, root, resourceType)

        return operator.toPredicate(
            criteriaBuilder,
            queryDialectHelper.getJsonValueExpression(criteriaBuilder, path, this.path, clazz),
            value
        )
    }

    private fun evaluateExpression(pathValue: V?): Boolean {
        return operator.evaluate(pathValue, value)
    }

    companion object {
        const val EXPRESSION = "expression"
    }
}