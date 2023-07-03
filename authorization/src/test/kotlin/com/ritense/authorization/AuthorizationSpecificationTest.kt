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

import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.testimpl.TestAuthorizationSpecification
import com.ritense.authorization.testimpl.TestEntity
import com.ritense.authorization.testimpl.TestEntityActionProvider
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthorizationSpecificationTest {

    @Test
    fun `isAuthorized should return true`() {
        val spec = TestAuthorizationSpecification(
            AuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.complete
            ),
            listOf(
                Permission(
                    resourceType = TestEntity::class.java,
                    action = TestEntityActionProvider.complete,
                    conditionContainer = ConditionContainer(listOf()),
                    role = Role(key = "")
                )
            )
        )

        assertEquals(true, spec.isAuthorized(TestEntity()))
    }

    @Test
    fun `isAuthorized should return false if no permission can be found for entity class`() {
        val spec = TestAuthorizationSpecification(
            AuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.complete
            ),
            listOf(
                Permission(
                    resourceType = String::class.java,
                    action = TestEntityActionProvider.complete,
                    conditionContainer = ConditionContainer(listOf()),
                    role = Role(key = "")
                )
            )
        ) as AuthorizationSpecification<Any>

        assertEquals(false, spec.isAuthorized(TestEntity()))
    }

    @Test
    fun `isAuthorized should return false if no permission can be found for requested action`() {
        val spec = TestAuthorizationSpecification(
            AuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.view
            ),
            listOf(
                Permission(
                    resourceType = TestEntity::class.java,
                    action = TestEntityActionProvider.complete,
                    conditionContainer = ConditionContainer(listOf()),
                    role = Role(key = "")
                )
            )
        )

        assertEquals(false, spec.isAuthorized(TestEntity()))
    }

    @Test
    fun `isAuthorized should return false when Permission_appliesTo() returns false`() {
        val permission: Permission = spy(Permission(
            resourceType = TestEntity::class.java,
            action = TestEntityActionProvider.complete,
            conditionContainer = ConditionContainer(listOf()),
            role = Role(key = ""))
        )
        val spec = TestAuthorizationSpecification(
            AuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.complete
            ),
            listOf(
                permission
            )
        )

        whenever(permission.appliesTo(eq(TestEntity::class.java), any())).thenReturn(false)

        val authorized = spec.isAuthorized(TestEntity())
        assertEquals(false, authorized)

        verify(permission).appliesTo(eq(TestEntity::class.java), any())
    }
}
