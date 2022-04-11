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

package com.ritense.formflow.expression

import com.ritense.formflow.exception.ExpressionExecutionException
import com.ritense.formflow.exception.ExpressionParseException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SpelExpressionProcessorTest {

    @Test
    fun `should validate SPeL expression`() {
        val expressionProcessor = SpelExpressionProcessor()

        expressionProcessor.validate("#{'Hello '+'World!'}")
    }

    @Test
    fun `should throw exception when error in syntax of expression`() {
        val expressionProcessor = SpelExpressionProcessor()

        assertThat(assertThrows<ExpressionParseException> {
            expressionProcessor.validate("#{'Hello +'World!'}")
        }.message).isEqualTo("Failed to parse expression: '#{'Hello +'World!'}'")
    }

    @Test
    fun `should return result when executing valid expression`() {
        val expressionProcessor = SpelExpressionProcessor()

        val result = expressionProcessor.process<Number>("#{3 / 1}")

        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `should throw exception when error while executing expression`() {
        val expressionProcessor = SpelExpressionProcessor()

        assertThat(assertThrows<ExpressionExecutionException> {
            expressionProcessor.process<Number>("#{3 / 0}")
        }.message).isEqualTo("Error while executing expression: '#{3 / 0}'")
    }
}