/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.authorization.permission

import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.ritense.authorization.jackson.ComparableDeserializer
import com.ritense.authorization.permission.ExpressionPermissionCondition.Companion.EXPRESSION
import com.ritense.valtimo.contract.database.QueryDialectHelper
import org.springframework.expression.spel.standard.SpelExpressionParser
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@JsonTypeName(EXPRESSION)
data class ExpressionPermissionCondition<V : Comparable<V>>(
    val field: String,
    val path: String,
    val operator: PermissionExpressionOperator,
    @JsonDeserialize(using = ComparableDeserializer::class)
    val value: V?,
    val clazz: Class<V>
) : ReflectingPermissionCondition(PermissionConditionType.EXPRESSION) {
    override fun <E : Any> isValid(entity: E): Boolean {
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

    override fun <E : Any> toPredicate(
        root: Root<E>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<E>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        val path: Path<Any>? = createDatabaseObjectPath(field, root)

        return toPredicate(
            criteriaBuilder,
            queryDialectHelper.getJsonValueExpression(criteriaBuilder, path, this.path, clazz),
            value
        )
    }

    private fun evaluateExpression(pathValue: V?): Boolean {
        return evaluate(pathValue, value)
    }


    private fun <T: Comparable<T>> evaluate(leftOperand: T?, rightOperand: T?): Boolean {
        val comparator = PermissionExpressionOperator.NullableComparator<T>(notEqualCompareResult)
        return SpelExpressionParser()
            .parseExpression(comparator.compare(leftOperand, rightOperand).toString() + this.asText + "0")
            .getValue(Boolean::class.java) ?: false
    }

    private fun <T : Comparable<T>> toPredicate(criteriaBuilder: CriteriaBuilder, expression: Expression<T>, value: T?): Predicate {
        return when(this) {
            PermissionExpressionOperator.NOT_EQUAL_TO ->
                criteriaBuilder.notEqual(
                    expression,
                    value
                )
            PermissionExpressionOperator.EQUAL_TO ->
                criteriaBuilder.equal(
                    expression,
                    value
                )
            PermissionExpressionOperator.LESS_THAN ->
                criteriaBuilder.lessThan(
                    expression,
                    value!!
                )
            PermissionExpressionOperator.LESS_THAN_OR_EQUAL_TO ->
                if(value == null) {
                    PermissionExpressionOperator.EQUAL_TO.toPredicate(criteriaBuilder, expression, null)
                } else {
                    criteriaBuilder.lessThanOrEqualTo(
                        expression,
                        value
                    )
                }
            PermissionExpressionOperator.GREATER_THAN ->
                criteriaBuilder.greaterThan(
                    expression,
                    value!!
                )
            PermissionExpressionOperator.GREATER_THAN_OR_EQUAL_TO ->
                if (value == null) {
                    PermissionExpressionOperator.EQUAL_TO.toPredicate(criteriaBuilder, expression, null)
                } else {
                    criteriaBuilder.greaterThanOrEqualTo(
                        expression,
                        value
                    )
                }
        }
    }

    companion object {
        const val EXPRESSION = "expression"
    }
}