/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.conditions

import PermissionConditionKey
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.ritense.valtimo.contract.authorization.CurrentUserExpressionHandler
import com.ritense.valtimo.contract.dashboard.ConditionSpelEvaluationContext
import com.ritense.valtimo.contract.repository.ExpressionOperator
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.time.LocalDateTime

/**
 * A condition that can generically be used for different features where user entered conditions are required.
 * Because the condition can be used in different contexts, the actual value resolution is done outside of this class.
 * The [expressionResolver] or [pathExpressionFunction] function is used to resolve the actual value of the field that
 * needs to be evaluated.
 */
data class Condition<T : Comparable<T>>(
    @JsonAlias("queryPath")
    val path: String,
    @JsonAlias("queryOperator")
    val operator: ExpressionOperator,
    @JsonAlias("queryValue")
    @JsonDeserialize(using = ComparableDeserializer::class)
    val value: T
) {

    fun isValid(
        expressionResolver: (String) -> Any
    ): Boolean {
        val fieldValue = expressionResolver.invoke(path)
        return operator.evaluate(fieldValue, value)
    }

    fun toPredicate(
        root: Root<*>,
        criteriaBuilder: CriteriaBuilder,
        pathExpressionFunction: (Class<Any>, String, Root<*>, CriteriaBuilder) -> Expression<Any>
    ): Predicate {
        pathExpressionFunction as (Class<T>, String, Root<*>, CriteriaBuilder) -> Expression<T>

        if (queryValueIsDateTimeSpelExpression(value)) {
            return getPredicateFromDateTimeSpelExpression(
                root,
                criteriaBuilder,
                pathExpressionFunction
            )
        }

        if (queryValueIsCurrentUserExpression(value)) {
            return getPredicateFromCurrentUserExpression(
                root,
                criteriaBuilder,
                pathExpressionFunction
            )
        }

        val predicateQueryValue: T? = if (queryValueIsNull(value)) {
            null
        } else {
            value
        }

        val valueClass = value::class.java as Class<T>

        val expression = pathExpressionFunction(valueClass, path, root, criteriaBuilder)

        return operator.toPredicate<T>(
            criteriaBuilder,
            expression,
            predicateQueryValue
        )
    }

    private fun <T> queryValueIsNull(target: T): Boolean {
        return target == "\${null}"
    }

    private fun queryValueIsDateTimeSpelExpression(target: Any?): Boolean {
        return (target as? String)?.let {
            it.isNotEmpty() && it.startsWith("\${") && it.endsWith('}') && "localDateTimeNow" in it
        } ?: false
    }

    private fun getPredicateFromDateTimeSpelExpression(
        root: Root<*>,
        criteriaBuilder: CriteriaBuilder,
        pathExpressionFunction: (Class<T>, String, Root<*>, CriteriaBuilder) -> Expression<T>
    ): Predicate {
        val condition = this as Condition<String>;
        val parser = SpelExpressionParser()
        val expressionWithoutPrefixSuffix = condition.value.substringAfter("\${").substringBefore("}")

        val spelEvaluationContext = ConditionSpelEvaluationContext()
        val context = StandardEvaluationContext()

        context.setRootObject(spelEvaluationContext)

        val spelExpression: org.springframework.expression.Expression =
            parser.parseExpression(expressionWithoutPrefixSuffix)

        val valueClass = LocalDateTime::class.java

        val value = spelExpression.getValue(context, valueClass)

        val expression =
            pathExpressionFunction(valueClass as Class<T>, condition.path, root, criteriaBuilder)

        return condition.operator.toPredicate<T>(
            criteriaBuilder,
            expression,
            value as T
        )
    }

    private fun <T> queryValueIsCurrentUserExpression(target: T): Boolean {
        return (target as? String)?.let {
            it.isNotEmpty() &&
                PermissionConditionKey.isValidKey(it) &&
                // roles is a list and is currently not supported
                PermissionConditionKey.fromKey(it) != PermissionConditionKey.CURRENT_USER_ROLES
        } ?: false
    }

    private fun getPredicateFromCurrentUserExpression(
        root: Root<*>,
        criteriaBuilder: CriteriaBuilder,
        pathExpressionFunction: (Class<T>, String, Root<*>, CriteriaBuilder) -> Expression<T>
    ): Predicate {
        val condition = this as Condition<String>;
        val valueClass = String::class.java
        val expression = pathExpressionFunction(valueClass as Class<T>, condition.path, root, criteriaBuilder)
        val permissionConditionKey = PermissionConditionKey.fromKey(condition.value)?.key
        val resolvedValue = CurrentUserExpressionHandler.resolveValue(permissionConditionKey) as? String ?: ""

        return condition.operator.toPredicate<T>(
            criteriaBuilder,
            expression,
            resolvedValue as T
        )
    }
}