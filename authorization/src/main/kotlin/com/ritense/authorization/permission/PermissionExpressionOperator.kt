package com.ritense.authorization.permission

import org.springframework.expression.spel.standard.SpelExpressionParser
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate

enum class PermissionExpressionOperator(val asText: String) {
    EQUAL_TO("=="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<=");

    fun <T: Comparable<T>> evaluate(leftOperand: T, rightOperand: T): Boolean {
        return SpelExpressionParser()
            .parseExpression(leftOperand.compareTo(rightOperand).toString() + this.asText + "0")
            .getValue(Boolean::class.java) ?: false
    }

    fun <T : Comparable<T>> toPredicate(criteriaBuilder: CriteriaBuilder, expression: Expression<T>, value: T): Predicate {
        return when(this) {
            EQUAL_TO ->
                criteriaBuilder.equal(
                    expression,
                    value
                )
            LESS_THAN ->
                criteriaBuilder.lessThan(
                    expression,
                    value
                )
            LESS_THAN_OR_EQUAL_TO ->
                criteriaBuilder.lessThanOrEqualTo(
                    expression,
                    value
                )
            GREATER_THAN ->
                criteriaBuilder.greaterThan(
                    expression,
                    value
                )
            GREATER_THAN_OR_EQUAL_TO ->
                criteriaBuilder.greaterThanOrEqualTo(
                    expression,
                    value
                )
        }
    }

}