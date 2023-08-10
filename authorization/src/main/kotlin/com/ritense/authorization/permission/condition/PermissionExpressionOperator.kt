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

package com.ritense.authorization.permission.condition

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.expression.spel.standard.SpelExpressionParser
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import kotlin.reflect.full.isSubclassOf

enum class PermissionExpressionOperator(
    @JsonValue val asText: String,
    private val notEqualCompareResult:Int = 1
) {
    NOT_EQUAL_TO("!="),
    EQUAL_TO("=="),
    GREATER_THAN(">", -1),
    GREATER_THAN_OR_EQUAL_TO(">=", -1),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<=");

    fun <T: Comparable<T>> evaluate(leftOperand: T?, rightOperand: T?): Boolean {
        val comparator = NullableComparator<T>(notEqualCompareResult)
        return SpelExpressionParser()
                .parseExpression(comparator.compare(leftOperand, rightOperand).toString() + this.asText + "0")
                .getValue(Boolean::class.java) ?: false
    }

    fun <T : Comparable<T>> toPredicate(criteriaBuilder: CriteriaBuilder, expression: Expression<T>, value: T?): Predicate {
        return when(this) {
            NOT_EQUAL_TO ->
                criteriaBuilder.notEqual(
                    expression,
                    value
                )
            EQUAL_TO ->
                criteriaBuilder.equal(
                    expression,
                    value
                )
            LESS_THAN ->
                criteriaBuilder.lessThan(
                    expression,
                    value!!
                )
            LESS_THAN_OR_EQUAL_TO ->
                if(value == null) {
                    EQUAL_TO.toPredicate(criteriaBuilder, expression, null)
                } else {
                    criteriaBuilder.lessThanOrEqualTo(
                        expression,
                        value
                    )
                }
            GREATER_THAN ->
                criteriaBuilder.greaterThan(
                    expression,
                    value!!
                )
            GREATER_THAN_OR_EQUAL_TO ->
                if (value == null) {
                    EQUAL_TO.toPredicate(criteriaBuilder, expression, null)
                } else {
                    criteriaBuilder.greaterThanOrEqualTo(
                        expression,
                        value
                    )
                }
        }
    }

    private class NullableComparator<T: Comparable<T>>(private val notEqualResult: Int): Comparator<T?> {
        override fun compare(left: T?, right: T?): Int {
            return if (left == right) {
                0
            } else if (
                left == null ||
                right == null ||
                (!left::class.isSubclassOf(right::class))
            ) {
                notEqualResult
            } else {
                left.compareTo(right)
            }
        }
    }
}