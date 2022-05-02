/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.formflow.expression.spel

import com.ritense.formflow.expression.ExpressionProcessor
import com.ritense.formflow.expression.ExpressionProcessorFactory
import org.springframework.context.expression.MapAccessor
import org.springframework.expression.spel.support.StandardEvaluationContext

class SpelExpressionProcessorFactory(
): ExpressionProcessorFactory {
    lateinit var formFlowBeans: Map<String, Any>
    override fun create(variables: Map<String, Any>?): ExpressionProcessor {
        val context = StandardEvaluationContext()
        context.addPropertyAccessor(MapAccessor())

        val contextMap: MutableMap<String, Any> = formFlowBeans.toMutableMap()

        variables?.let {
            contextMap.putAll(variables)
        }

        return SpelExpressionProcessor(evaluationContext = context, contextMap = contextMap)
    }

    override fun setFlowProcessBeans(formFlowBeans: Map<String, Any>) {
        this.formFlowBeans = formFlowBeans
    }
}