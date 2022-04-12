package com.ritense.formflow.expression.spel

import com.ritense.formflow.expression.ExpressionProcessor
import com.ritense.formflow.expression.ExpressionProcessorFactory
import org.springframework.expression.spel.support.StandardEvaluationContext

class SpelExpressionProcessorFactory: ExpressionProcessorFactory {
    override fun create(rootObject: Any?): ExpressionProcessor {
        val context = StandardEvaluationContext(rootObject)
        return SpelExpressionProcessor(evaluationContext = context)
    }
}