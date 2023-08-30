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
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import kotlin.reflect.full.isSubclassOf

enum class PermissionConditionOperator(@JsonValue val asText: String) {
    NOT_EQUAL_TO("!="),
    EQUAL_TO("=="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<="),
    CONTAINS("contains");

    fun evaluate(left: Any?, right: Any?): Boolean {
        return when (this) {
            NOT_EQUAL_TO -> left != right
            EQUAL_TO -> left == right
            GREATER_THAN -> compare(left, right, -1) > 0
            GREATER_THAN_OR_EQUAL_TO -> compare(left, right, -1) >= 0
            LESS_THAN -> compare(left, right) < 0
            LESS_THAN_OR_EQUAL_TO -> compare(left, right) <= 0
            CONTAINS -> contains(left, right)
        }
    }

    fun <T : Comparable<T>> toPredicate(
        criteriaBuilder: CriteriaBuilder,
        expression: Expression<*>,
        value: Any?
    ): Predicate {
        return when (this) {
            NOT_EQUAL_TO ->
                criteriaBuilder.notEqual(expression, value)

            EQUAL_TO ->
                criteriaBuilder.equal(expression, value)

            LESS_THAN -> {
                criteriaBuilder.lessThan(expression as Expression<T>, value!! as T)
            }

            LESS_THAN_OR_EQUAL_TO ->
                if (value == null) {
                    criteriaBuilder.isNull(expression)
                } else {
                    criteriaBuilder.lessThanOrEqualTo(expression as Expression<T>, value as T)
                }

            GREATER_THAN ->
                criteriaBuilder.greaterThan(expression as Expression<T>, value!! as T)

            GREATER_THAN_OR_EQUAL_TO ->
                if (value == null) {
                    criteriaBuilder.isNull(expression)
                } else {
                    criteriaBuilder.greaterThanOrEqualTo(expression as Expression<T>, value as T)
                }

            CONTAINS ->
                criteriaBuilder.literal(value).`in`(expression as Expression<Collection<T>>)
        }
    }

    private fun compare(left: Any?, right: Any?, notEqualResult: Int = 1): Int {
        return if (left == right) {
            0
        } else if (left == null || right == null || (!left::class.isSubclassOf(right::class))) {
            notEqualResult
        } else if (left is Comparable<*> && right is Comparable<*>) {
            (left as Comparable<Any>).compareTo(right as Any)
        } else {
            notEqualResult
        }
    }

    private fun contains(collection: Any?, value: Any?): Boolean {
        return if (collection == value) {
            true
        } else if (collection != null && collection is Collection<*>) {
            collection.contains(value)
        } else {
            false
        }
    }
}
