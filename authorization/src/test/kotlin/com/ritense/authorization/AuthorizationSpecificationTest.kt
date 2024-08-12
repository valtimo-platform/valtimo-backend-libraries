/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.authorization.testimpl.TestAuthorizationSpecification
import com.ritense.authorization.testimpl.TestEntity
import com.ritense.authorization.testimpl.TestEntityActionProvider
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthorizationSpecificationTest {

    @Test
    fun `isAuthorized should return true`() {
        val spec = TestAuthorizationSpecification(
            EntityAuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.complete, TestEntity()
            ),
            listOf(
                Permission(
                    resourceType = TestEntity::class.java,
                    action = TestEntityActionProvider.complete,
                    conditionContainer = ConditionContainer(listOf()),
                    role = Role(key = "")
                )
            ),
            mock()
        )

        assertEquals(true, spec.isAuthorized())
    }

    @Test
    fun `isAuthorized should return false if no permission can be found for entity class`() {
        val spec = TestAuthorizationSpecification(
            EntityAuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.complete, TestEntity()
            ),
            listOf(
                Permission(
                    resourceType = String::class.java,
                    action = TestEntityActionProvider.complete,
                    conditionContainer = ConditionContainer(listOf()),
                    role = Role(key = "")
                )
            ),
            mock()
        ) as AuthorizationSpecification<Any>

        assertEquals(false, spec.isAuthorized())
    }

    @Test
    fun `isAuthorized should return false if no permission can be found for requested action`() {
        val spec = TestAuthorizationSpecification(
            EntityAuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.view, TestEntity()
            ),
            listOf(
                Permission(
                    resourceType = TestEntity::class.java,
                    action = TestEntityActionProvider.complete,
                    conditionContainer = ConditionContainer(listOf()),
                    role = Role(key = "")
                )
            ),
            mock()
        )

        assertEquals(false, spec.isAuthorized())
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
            EntityAuthorizationRequest(
                TestEntity::class.java, action = TestEntityActionProvider.complete, TestEntity()
            ),
            listOf(
                permission
            ),
            mock()
        )

        whenever(permission.appliesTo(eq(TestEntity::class.java), any(), eq(null), eq(null))).thenReturn(false)

        val authorized = spec.isAuthorized()
        assertEquals(false, authorized)

        verify(permission).appliesTo(eq(TestEntity::class.java), any(), eq(null), eq(null))
    }
}
