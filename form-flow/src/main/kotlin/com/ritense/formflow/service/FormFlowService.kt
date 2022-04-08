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

package com.ritense.formflow.service

import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.exception.FormFlowExpressionExecutionException
import com.ritense.formflow.exception.FormFlowExpressionParseException
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.springframework.expression.Expression
import org.springframework.expression.ParseException
import org.springframework.expression.ParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser

class FormFlowService(
    private val formFlowDefinitionRepository: FormFlowDefinitionRepository,
    private val formFlowInstanceRepository: FormFlowInstanceRepository
) {

    fun findLatestDefinitionByKey(formFlowKey: String): FormFlowDefinition? {
        return formFlowDefinitionRepository.findFirstByIdKeyOrderByIdVersionDesc(formFlowKey)
    }

    fun getDefinitionById(formFlowDefinitionId: FormFlowDefinitionId): FormFlowDefinition {
        return formFlowDefinitionRepository.getById(formFlowDefinitionId)
    }

    fun open(formFlowInstance: FormFlowInstance) {
        val step = formFlowInstance.formFlowDefinition.getStepByKey(formFlowInstance.getCurrentStep().stepKey)

        step.onOpen?.forEach { expression ->
            val spelExpression = parseSpelExpression(expression)

            if (spelExpression != null) {
                try {
                    spelExpression.value
                } catch (e: RuntimeException) {
                    throw FormFlowExpressionExecutionException(
                        formFlowInstance.formFlowDefinition.id.key,
                        expression,
                        e
                    )
                }
            }
        }
    }

    fun parseSpelExpression(expression: String, context: ParserContext? = null): Expression? {
        val expressionMatcher = "^\\\$\\{(.*)\\}\$".toRegex().find(expression) ?: return null
        val parser = SpelExpressionParser()
        try {
            return parser.parseExpression(expressionMatcher.groupValues[1], context)
        } catch (e: ParseException) {
            throw FormFlowExpressionParseException(expression, e)
        }
    }

    fun save(formFlowDefinition: FormFlowDefinition) {
        formFlowDefinitionRepository.findById(formFlowDefinition.id).ifPresentOrElse({
            throw UnsupportedOperationException("Failed to save From Flow. Form Flow already exists: ${formFlowDefinition.id}")
        }, {
            formFlowDefinitionRepository.save(formFlowDefinition)
        })
    }

    fun getInstanceById(formFlowInstanceId: FormFlowInstanceId): FormFlowInstance {
        return formFlowInstanceRepository.getById(formFlowInstanceId)
    }

    fun save(formFlowInstance: FormFlowInstance) {
        formFlowInstanceRepository.save(formFlowInstance)
    }
}