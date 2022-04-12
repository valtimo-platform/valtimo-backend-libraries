package com.ritense.formflow.expression

interface ExpressionProcessor {
    fun <T> process(expression: String, resultType: Class<T>? = null): T?
    fun validate(expression: String)
}