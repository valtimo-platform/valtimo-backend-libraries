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

import com.ritense.authorization.testimpl.TestChildEntity
import com.ritense.authorization.testimpl.TestEntity
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

class FieldPermissionConditionTest {
    //TODO: The entity or child objects can't be a Map, don't we want to support this?
    private val entity = TestEntity(
        TestChildEntity(true)
    )

    @Test
    fun `should find property and pass the condition`() {
        val condition = FieldPermissionCondition("child.property", "true")
        val result = condition.isValid(entity)
        assertEquals(true, result)
    }

    @Test
    fun `should find property and fail the condition`() {
        val condition = FieldPermissionCondition("child.property", "false")
        val result = condition.isValid(entity)
        assertEquals(false, result)
    }

    @Test
    fun `should not find property and throw exception`() {
        val condition = FieldPermissionCondition("child.non-existent", "true")
        assertThrows<NoSuchFieldException> {
            condition.isValid(entity)
        }
    }
}