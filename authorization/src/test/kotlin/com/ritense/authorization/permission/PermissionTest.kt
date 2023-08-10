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

import com.ritense.authorization.Action
import com.ritense.authorization.permission.condition.PermissionCondition
import com.ritense.authorization.role.Role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PermissionTest {

    lateinit var condition1: PermissionCondition
    lateinit var condition2: PermissionCondition
    lateinit var permission: Permission

    @BeforeEach
    fun setup() {
        condition1 = mock()
        condition2 = mock()
        permission = spy(
            Permission(
                resourceType = String::class.java,
                action = Action<String>(Action.VIEW),
                conditionContainer = ConditionContainer(listOf(
                    condition1,
                    condition2
                )),
                role = Role(key = "")
            )
        )
    }

    @Test
    fun `should apply to valid resource type, entity type and condition`() {
        val entity = ""
        whenever(condition1.isValid(entity)).thenReturn(true)
        whenever(condition2.isValid(entity)).thenReturn(true)
        val result = permission.appliesTo(String::class.java, entity)
        assertEquals(true, result)

        verify(condition1).isValid(entity)
        verify(condition2).isValid(entity)
    }

    @Test
    fun `should not apply when any condition fails`() {
        val entity = ""
        whenever(condition1.isValid(entity)).thenReturn(true)
        val result = permission.appliesTo(String::class.java, entity)
        assertEquals(false, result)

        verify(condition1).isValid(entity)
        verify(condition2).isValid(entity)
    }

    @Test
    fun `should not apply to invalid resource type`() {
        val result = permission.appliesTo(Int::class.java, "")
        assertEquals(false, result)
        verify(condition1, never()).isValid(any())
    }

    @Test
    fun `should not apply to null entity value`() {
        val result = permission.appliesTo(String::class.java, null)
        assertEquals(false, result)
        verify(condition1, never()).isValid(any())
    }
}