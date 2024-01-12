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

package com.ritense.valtimo

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import org.camunda.bpm.engine.impl.javax.el.ELContext
import org.camunda.bpm.engine.impl.javax.el.ValueExpression
import org.camunda.bpm.engine.spring.SpringExpressionManager
import org.springframework.context.ApplicationContext

class ValtimoExpressionManager(
    applicationContext: ApplicationContext,
    beans: Map<Any, Any>
): SpringExpressionManager(applicationContext, beans) {
    override fun createValueExpression(expression: String?): ValueExpression {
        ensureInitialized()
        val expression = expressionFactory.createValueExpression(parsingElContext, expression, Any::class.java)
        return ValtimoValueExpression(expression)
    }
}

class ValtimoValueExpression(
    private val expression: ValueExpression
): ValueExpression() {
    override fun equals(other: Any?): Boolean {
        return expression.equals(other)
    }

    override fun hashCode(): Int {
        return expression.hashCode()
    }

    override fun getExpressionString(): String {
        return expression.expressionString
    }

    override fun isLiteralText(): Boolean {
        return expression.isLiteralText
    }

    override fun getExpectedType(): Class<*> {
        return expression.expectedType
    }

    override fun getType(context: ELContext?): Class<*> {
        return expression.getType(context)
    }

    override fun getValue(context: ELContext?): Any? {
        return runWithoutAuthorization {
            expression.getValue(context)
        }
    }

    override fun isReadOnly(context: ELContext?): Boolean {
        return expression.isReadOnly(context)
    }

    override fun setValue(context: ELContext?, value: Any?) {
        return expression.setValue(context, value)
    }
}