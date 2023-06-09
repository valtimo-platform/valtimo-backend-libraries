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

package com.ritense.authorization

import com.ritense.authorization.permission.Permission
import com.ritense.authorization.testimpl.TestEntity
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException

class AuthorizationServiceIntTest @Autowired constructor(
    private val authorizationService: AuthorizationService
) : BaseIntegrationTest() {
    @Test
    fun `should succeed when ran without authorization`() {
        assertDoesNotThrow {
            AuthorizationContext.runWithoutAuthorization {
                requirePermission()
            }
        }
    }

    @Test
    fun `should throw RuntimeException when ran with authorization`() {
        assertThrows<AccessDeniedException> {
            requirePermission()
        }
    }

    @Test
    fun `should throw RuntimeException when action is DENY`() {
        assertThrows<AccessDeniedException> {
            requirePermission(Action.deny())
        }
    }

    @Test
    fun `should succeed when ran without authorization when action is DENY`() {
        assertDoesNotThrow {
            AuthorizationContext.runWithoutAuthorization {
                requirePermission(Action.deny())
            }
        }
    }

    @Test
    fun `should pass permission check when entity is not null`() {
        val permission: Permission = mock()
        doReturn(Action<TestEntity>(Action.VIEW)).whenever(permission).action
        doReturn(TestEntity::class.java).whenever(permission).resourceType
        doReturn(true).whenever(permission).appliesTo(eq(TestEntity::class.java), any())

        assertDoesNotThrow {
            requirePermission(
                entity = TestEntity(),
                permission = permission)
        }

        verify(permission).appliesTo(eq(TestEntity::class.java), any())
    }

    @Test
    fun `should fail permission check when entity is null`() {
        val permission: Permission = mock()

        assertThrows<AccessDeniedException> {
            requirePermission(
                permission = permission
            )
        }

        verify(permission, never()).appliesTo(eq(TestEntity::class.java), any())
    }

    @Test
    fun `should find all available actions for entity`() {
        val allActions = authorizationService.getAvailableActionsForResource(TestEntity::class.java)
        assertThat(
            allActions,
            hasItems(
                Action(Action.VIEW),
                Action(Action.COMPLETE),
                Action(Action.MODIFY),
                Action("custom")
            )
        )
    }

    fun requirePermission(action: Action<TestEntity> = Action(Action.VIEW)) {
        requirePermission(
            action = action,
            entity = TestEntity())
    }

    fun requirePermission(
        action: Action<TestEntity> = Action(Action.VIEW),
        entity: TestEntity? = null,
        permission: Permission? = null
    ) {
        authorizationService
            .requirePermission(
                AuthorizationRequest(
                    TestEntity::class.java,
                    action = action),
                entity,
                permission?.let { listOf(it) }
            )
    }
}
