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
    fun `NOT_EQUAL_TO should evaluate correctly`() {
        val op = PermissionExpressionOperator.NOT_EQUAL_TO
        assertEquals(true, op, "a", "b")
        assertEquals(false, op, "b", "b")
        assertEquals(true, op, "b", "a")
        assertEquals(true, op, 1.35, 1.34)
        assertEquals(false, op, 1.34, 1.34)
        assertEquals(true, op, 1.33, 1.34)
        assertEquals(false, op, null, null)
        assertEquals(true, op, null, "a")
        assertEquals(true, op, "a", null)
    }

    @Test
    fun `EQUAL_TO should evaluate correctly`() {
        val op = PermissionExpressionOperator.EQUAL_TO
        assertEquals(false, op, "a", "b")
        assertEquals(true, op, "b", "b")
        assertEquals(false, op, "b", "a")
        assertEquals(false, op, 1.35, 1.34)
        assertEquals(true, op, 1.34, 1.34)
        assertEquals(false, op, 1.33, 1.34)
        assertEquals(true, op, null, null)
        assertEquals(false, op, null, "a")
        assertEquals(false, op, "a", null)
    }

    @Test
    fun `GREATER_THAN should evaluate correctly`() {
        val op = PermissionExpressionOperator.GREATER_THAN

        assertEquals(false, op, "a", "b")
        assertEquals(false, op, "b", "b")
        assertEquals(true, op, "b", "a")
        assertEquals(true, op, 1.35, 1.34)
        assertEquals(false, op, 1.34, 1.34)
        assertEquals(false, op, 1.33, 1.34)
        assertEquals(false, op, null, null)
        assertEquals(false, op, null, "a")
        assertEquals(false, op, "a", null)
    }

    @Test
    fun `GREATER_THAN_OR_EQUAL_TO should evaluate correctly`() {
        val op = PermissionExpressionOperator.GREATER_THAN_OR_EQUAL_TO

        assertEquals(false, op, "a", "b")
        assertEquals(true, op, "b", "b")
        assertEquals(true, op, "b", "a")
        assertEquals(true, op, 1.35, 1.34)
        assertEquals(true, op, 1.34, 1.34)
        assertEquals(false, op, 1.33, 1.34)
        assertEquals(true, op, null, null)
        assertEquals(false, op, null, "a")
        assertEquals(false, op, "a", null)
    }

    @Test
    fun `LESS_THAN should evaluate correctly`() {
        val op = PermissionExpressionOperator.LESS_THAN

        assertEquals(true, op, "a", "b")
        assertEquals(false, op, "b", "b")
        assertEquals(false, op, "b", "a")
        assertEquals(false, op, 1.35, 1.34)
        assertEquals(false, op, 1.34, 1.34)
        assertEquals(true, op, 1.33, 1.34)
        assertEquals(false, op, null, null)
        assertEquals(false, op, null, "a")
        assertEquals(false, op, "a", null)
    }

    @Test
    fun `LESS_THAN_OR_EQUAL_TO should evaluate correctly`() {
        val op = PermissionExpressionOperator.LESS_THAN_OR_EQUAL_TO

        assertEquals(true, op, "a", "b")
        assertEquals(true, op, "b", "b")
        assertEquals(false, op, "b", "a")
        assertEquals(false, op, 1.35, 1.34)
        assertEquals(true, op, 1.34, 1.34)
        assertEquals(true, op, 1.33, 1.34)
        assertEquals(true, op,null, null)
        assertEquals(false, op, null, "a")
        assertEquals(false, op, "a", null)
    }

    fun <T: Comparable<T>> assertEquals(expected: Boolean, op: PermissionExpressionOperator, left: T?, right: T?) {
        assertEquals(expected, op.evaluate(left, right), "[ $left ${op.asText} $right ]")
    }
}