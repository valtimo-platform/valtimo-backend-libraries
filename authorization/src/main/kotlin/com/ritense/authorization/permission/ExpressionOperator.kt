package com.ritense.authorization.permission

enum class ExpressionOperator(val asText: String, val function: (Int) -> Boolean,) {
    EQUAL_TO("==", { value: Int -> equalTo(value) }),
    LESS_THAN("<", { value: Int -> lessThan(value) }),
    LESS_THAN_OR_EQUAL_TO("<=", { value: Int -> lessThanOrEqualTo(value) }),
    GREATER_THAN(">", { value: Int -> greaterThan(value) }),
    GREATER_THAN_OR_EQUAL_TO(">=", { value: Int -> greaterThanOrEqualTo(value) });

    fun <T: Comparable<T>> evaluate(value1: T, value2: T): Boolean {
        return this.function.invoke(value1.compareTo(value2))
    }

}

private fun equalTo(value: Int): Boolean {
    return value == 0
}

private fun lessThan(value: Int): Boolean {
    return value < 0
}

private fun lessThanOrEqualTo(value: Int): Boolean {
    return value <= 0
}
private fun greaterThan(value: Int): Boolean {
    return value > 0
}

private fun greaterThanOrEqualTo(value: Int): Boolean {
    return value >= 0
}