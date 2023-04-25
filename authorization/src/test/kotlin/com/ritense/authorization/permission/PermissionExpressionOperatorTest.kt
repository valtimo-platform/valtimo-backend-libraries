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

package com.ritense.authorization.permission

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class PermissionExpressionOperatorTest {
    @Test
    fun `EQUAL_TO should evaluate correctly`() {
        val op = PermissionExpressionOperator.EQUAL_TO
        assertEquals(false, op.evaluate("a", "b"))
        assertEquals(true, op.evaluate("b", "b"))
        assertEquals(false, op.evaluate("b", "a"))
        assertEquals(false, op.evaluate(1.35, 1.34))
        assertEquals(true, op.evaluate(1.34, 1.34))
        assertEquals(false, op.evaluate(1.33, 1.34))
    }

    @Test
    fun `GREATER_THAN should evaluate correctly`() {
        val op = PermissionExpressionOperator.GREATER_THAN

        assertEquals(false, op.evaluate("a", "b"))
        assertEquals(false, op.evaluate("b", "b"))
        assertEquals(true, op.evaluate("b", "a"))
        assertEquals(true, op.evaluate(1.35, 1.34))
        assertEquals(false, op.evaluate(1.34, 1.34))
        assertEquals(false, op.evaluate(1.33, 1.34))
    }

    @Test
    fun `GREATER_THAN_OR_EQUAL_TO should evaluate correctly`() {
        val op = PermissionExpressionOperator.GREATER_THAN_OR_EQUAL_TO

        assertEquals(false, op.evaluate("a", "b"))
        assertEquals(true, op.evaluate("b", "b"))
        assertEquals(true, op.evaluate("b", "a"))
        assertEquals(true, op.evaluate(1.35, 1.34))
        assertEquals(true, op.evaluate(1.34, 1.34))
        assertEquals(false, op.evaluate(1.33, 1.34))
    }

    @Test
    fun `LESS_THAN should evaluate correctly`() {
        val op = PermissionExpressionOperator.LESS_THAN

        assertEquals(true, op.evaluate("a", "b"))
        assertEquals(false, op.evaluate("b", "b"))
        assertEquals(false, op.evaluate("b", "a"))
        assertEquals(false, op.evaluate(1.35, 1.34))
        assertEquals(false, op.evaluate(1.34, 1.34))
        assertEquals(true, op.evaluate(1.33, 1.34))
    }

    @Test
    fun `LESS_THAN_OR_EQUAL_TO should evaluate correctly`() {
        val op = PermissionExpressionOperator.LESS_THAN_OR_EQUAL_TO

        assertEquals(true, op.evaluate("a", "b"))
        assertEquals(true, op.evaluate("b", "b"))
        assertEquals(false, op.evaluate("b", "a"))
        assertEquals(false, op.evaluate(1.35, 1.34))
        assertEquals(true, op.evaluate(1.34, 1.34))
        assertEquals(true, op.evaluate(1.33, 1.34))
    }
}