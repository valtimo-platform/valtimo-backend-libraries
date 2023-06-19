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
import com.ritense.authorization.specification.DenyAuthorizationSpecification
import com.ritense.authorization.specification.DenyAuthorizationSpecificationFactory
import com.ritense.authorization.specification.NoopAuthorizationSpecification
import com.ritense.authorization.specification.NoopAuthorizationSpecificationFactory
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.access.AccessDeniedException

class AuthorizationServiceTest {

    lateinit var factory1: AuthorizationSpecificationFactory<Int>
    lateinit var factory2: AuthorizationSpecificationFactory<String>
    lateinit var factory3: AuthorizationSpecificationFactory<Boolean>
    lateinit var mapper1: AuthorizationEntityMapper<Int, Int>
    lateinit var mapper2: AuthorizationEntityMapper<String, String>
    lateinit var mapper3: AuthorizationEntityMapper<Boolean, Boolean>
    lateinit var permissionRepository: PermissionRepository
    lateinit var authorizationService: AuthorizationService

    @BeforeEach
    fun setup() {
        factory1 = mock()
        factory2 = mock()
        factory3 = mock()
        mapper1 = mock()
        mapper2 = mock()
        mapper3 = mock()
        permissionRepository = mock()

        authorizationService = ValtimoAuthorizationService(
            listOf(
                NoopAuthorizationSpecificationFactory(),
                DenyAuthorizationSpecificationFactory(),
                factory1,
                factory2,
                factory3
            ),
            listOf(
                mapper1,
                mapper2,
                mapper3
            ),
            listOf(
                StringTestActionProvider(),
                OtherStringTestActionProvider(),
                IntTestActionProvider()
            ),
            permissionRepository
        )

    }

    @Test
    fun `should pass permission check`() {
        whenever(factory2.canCreate(any(), any())).thenReturn(true)
        val context = AuthorizationRequest(String::class.java, action = Action(Action.VIEW))
        val authorizationSpecification = mock<AuthorizationSpecification<String>>()
        whenever(factory2.create(eq(context), any())).thenReturn(authorizationSpecification)
        val entity = ""
        whenever(authorizationSpecification.isAuthorized(entity)).thenReturn(true)

        authorizationService.requirePermission(context, entity,
            listOf(
                Permission(
                    resourceType = String::class.java,
                    action = Action<String>(Action.VIEW),
                    conditionContainer = ConditionContainer(), roleKey = ""
                )
            )
        )

        verify(authorizationSpecification).isAuthorized(entity)
    }

    @Test
    fun `should bypass permission check`() {
        whenever(factory2.canCreate(any(), any())).thenReturn(true)
        val context = AuthorizationRequest(String::class.java, action = Action(Action.VIEW))
        val authorizationSpecification = mock<AuthorizationSpecification<String>>()
        whenever(factory2.create(context, listOf())).thenReturn(authorizationSpecification)
        val entity = ""

        AuthorizationContext.runWithoutAuthorization {
            authorizationService.requirePermission(context, entity)
        }

        verify(authorizationSpecification, never()).isAuthorized(entity)
    }

    @Test
    fun `should fail permission check`() {
        whenever(factory2.canCreate(any(), any())).thenReturn(true)
        val context = AuthorizationRequest(String::class.java, action = Action(Action.VIEW))
        val authorizationSpecification = mock<AuthorizationSpecification<String>>()
        whenever(factory2.create(eq(context), any())).thenReturn(authorizationSpecification)
        val entity = ""
        whenever(authorizationSpecification.isAuthorized(entity)).thenReturn(false)

        assertThrows<RuntimeException> {
            authorizationService.requirePermission(context, entity,
                listOf(
                    Permission(
                        resourceType = String::class.java,
                        action = Action<String>(Action.VIEW),
                        conditionContainer = ConditionContainer(), roleKey = ""
                    )
                )
            )
        }

        verify(authorizationSpecification).isAuthorized(entity)
    }

    @Test
    fun `should get correct AuthorizationSpecification`() {
        whenever(factory2.canCreate(any(), any())).thenReturn(true)
        whenever(factory3.canCreate(any(), any())).thenReturn(true)


        val context = AuthorizationRequest(String::class.java, action = Action(Action.VIEW))
        val authorizationSpecification = mock<AuthorizationSpecification<String>>()
        whenever(factory2.create(eq(context), any())).thenReturn(authorizationSpecification)
        val result = authorizationService.getAuthorizationSpecification(context,
            listOf(
                Permission(
                    resourceType = String::class.java,
                    action = Action<String>(Action.VIEW),
                    conditionContainer = ConditionContainer(), roleKey = ""
                )
            )
        )
        assertEquals(authorizationSpecification, result)

        verify(factory1).canCreate(any(), any())
        verify(factory2).canCreate(any(), any())
        verify(factory3, never()).canCreate(any(), any())
        verify(factory2).create(eq(context), any())
    }

    @Test
    fun `should get NoopAuthorizationSpecification`() {
        whenever(factory1.canCreate(any(), any())).thenReturn(true)
        whenever(factory2.canCreate(any(), any())).thenReturn(true)

        val context = AuthorizationRequest(String::class.java, action = Action(Action.VIEW))
        val result = AuthorizationContext.runWithoutAuthorization {
            authorizationService.getAuthorizationSpecification(context)
        }
        assertEquals(true, result is NoopAuthorizationSpecification)

        verify(factory1, never()).canCreate(any(), any())
        verify(factory2, never()).canCreate(any(), any())
    }

    @Test
    fun `should get DenyAuthorizationSpecification on DENY action`() {
        whenever(factory1.canCreate(any(), any())).thenReturn(true)
        whenever(factory2.canCreate(any(), any())).thenReturn(true)

        val context = AuthorizationRequest(String::class.java, action = Action(Action.DENY))
        val result = authorizationService.getAuthorizationSpecification(context)
        assertEquals(true, result is DenyAuthorizationSpecification)

        verify(factory1, never()).canCreate(any(), any())
        verify(factory2, never()).canCreate(any(), any())
    }

    @Test
    fun `should get DenyAuthorizationSpecification when permission list is empty`() {
        whenever(factory1.canCreate(any(), any())).thenReturn(true)
        whenever(factory2.canCreate(any(), any())).thenReturn(true)

        val context = AuthorizationRequest(String::class.java, action = Action(Action.VIEW))
        val result = authorizationService.getAuthorizationSpecification(context, emptyList())
        assertEquals(true, result is DenyAuthorizationSpecification)

        verify(factory1, never()).canCreate(any(), any())
        verify(factory2, never()).canCreate(any(), any())
    }

    @Test
    fun `should throw an error when no correct AuthorizationSpecification can be found`() {
        assertThrows<AccessDeniedException> {
            val context = AuthorizationRequest(String::class.java, action = Action(Action.VIEW))
            authorizationService.getAuthorizationSpecification(context,
                listOf(
                    Permission(
                        resourceType = String::class.java,
                        action = Action<String>(Action.VIEW),
                        conditionContainer = ConditionContainer(), roleKey = ""
                    )
                )
            )
        }
        verify(factory1).canCreate(any(), any())
        verify(factory2).canCreate(any(), any())
        verify(factory3).canCreate(any(), any())
    }

    @Test
    fun `should get correct AuthorizationEntityMapper`() {
        whenever(mapper2.supports(any(), any())).thenReturn(true)
        whenever(mapper3.supports(any(), any())).thenReturn(true)


        val mapper = authorizationService.getMapper(String::class.java, String::class.java)
        assertEquals(mapper2, mapper)

        verify(mapper1).supports(any(), any())
        verify(mapper2).supports(any(), any())
        verify(mapper3, never()).supports(any(), any())
    }

    @Test
    fun `should throw an error when no correct AuthorizationEntityMapper can be found`() {
        assertThrows<AccessDeniedException> {
            authorizationService.getMapper(String::class.java, String::class.java)
        }

        verify(mapper1).supports(any(), any())
        verify(mapper2).supports(any(), any())
        verify(mapper3).supports(any(), any())
    }

    @Test
    fun `should get available actions only for type that is requested`() {
        val availableActionsForResource = authorizationService.getAvailableActionsForResource(String::class.java)
        assertEquals(2, availableActionsForResource.size)
        assertEquals(Action.VIEW, availableActionsForResource[0].key)
        assertEquals(Action.CLAIM, availableActionsForResource[1].key)
    }
}

class StringTestActionProvider(): ResourceActionProvider<String> {
    override fun getAvailableActions(): List<Action<String>> {
        return listOf(Action(Action.VIEW))
    }
}

class OtherStringTestActionProvider(): ResourceActionProvider<String> {
    override fun getAvailableActions(): List<Action<String>> {
        return listOf(Action(Action.CLAIM))
    }
}

class IntTestActionProvider(): ResourceActionProvider<Int> {
    override fun getAvailableActions(): List<Action<Int>> {
        return listOf(Action(Action.MODIFY))
    }
}