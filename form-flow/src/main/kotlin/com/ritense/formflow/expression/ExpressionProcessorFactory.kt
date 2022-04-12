package com.ritense.formflow.expression

interface ExpressionProcessorFactory {
    fun create(rootObject: Any?): ExpressionProcessor
}