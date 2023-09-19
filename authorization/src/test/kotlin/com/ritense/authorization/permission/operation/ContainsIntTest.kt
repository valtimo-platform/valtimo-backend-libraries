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

package com.ritense.authorization.permission.operation

import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.testimpl.TestChildEntity
import com.ritense.authorization.testimpl.TestEntity
import com.ritense.authorization.testimpl.TestEntityActionProvider.Companion.view
import com.ritense.authorization.testimpl.TestEntityActionProvider.Companion.view_list
import com.ritense.authorization.testimpl.TestEntityRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import kotlin.test.assertEquals

class ContainsIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var repository: TestEntityRepository

    @Autowired
    lateinit var authorizationService: AuthorizationService

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    @WithMockUser(authorities = ["EXPRESSION_CONTAINS_ROLE"])
    fun `should find entity where json array contains value in permission`() {
        val testEntity = TestEntity(
            TestChildEntity(listOf("one", "two", "three")),
            "henk"
        )

        repository.save(testEntity)

        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view_list,
                null
            )
        )

        val entities = repository.findAll(spec)

        assertEquals(1, entities.size)
        assertEquals("henk", entities[0].name)
    }

    @Test
    @WithMockUser(authorities = ["EXPRESSION_CONTAINS_ROLE"])
    fun `should not find entity where json array does not contain value in permission`() {
        val testEntity = TestEntity(
            TestChildEntity(listOf("one", "three")),
            "henk"
        )

        repository.save(testEntity)

        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view_list,
                null
            )
        )

        val entities = repository.findAll(spec)

        assertEquals(0, entities.size)
    }

    @Test
    @WithMockUser(authorities = ["EXPRESSION_CONTAINS_ROLE"])
    fun `should have permissions for entity where json array contains value in permission`() {
        val testEntity = TestEntity(
            TestChildEntity(listOf("one", "two", "three")),
            "henk"
        )

        val hasPermission = authorizationService.hasPermission(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view,
                testEntity
            )
        )

        assertTrue(hasPermission)
    }

    @Test
    @WithMockUser(authorities = ["EXPRESSION_CONTAINS_ROLE"])
    fun `should not have permissions for entity where json array contains value in permission`() {
        val testEntity = TestEntity(
            TestChildEntity(listOf("one", "three")),
            "henk"
        )

        val hasPermission = authorizationService.hasPermission(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view,
                testEntity
            )
        )

        assertFalse(hasPermission)
    }

    @Test
    @WithMockUser(authorities = ["FIELD_CONTAINS_ROLE"])
    fun `should find entity where field list contains value in permission`() {
        val testEntity = TestEntity(
            name = "henk",
            fruits = mutableListOf("peer", "strawberry", "banana")
        )

        repository.save(testEntity)

        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view_list,
                null
            )
        )

        val entities = repository.findAll(spec)

        assertEquals(1, entities.size)
        assertEquals("henk", entities[0].name)
    }

    @Test
    @WithMockUser(authorities = ["FIELD_CONTAINS_ROLE"])
    fun `should not find entity where field list does not contain value in permission`() {
        val testEntity = TestEntity(
            name = "henk",
            fruits = mutableListOf("peer", "banana")
        )

        repository.save(testEntity)

        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view_list,
                null
            )
        )

        val entities = repository.findAll(spec)

        assertEquals(0, entities.size)
    }

    @Test
    @WithMockUser(authorities = ["FIELD_CONTAINS_ROLE"])
    fun `should have permission for entity where field list contains value in permission`() {
        val testEntity = TestEntity(
            name = "henk",
            fruits = mutableListOf("peer", "strawberry", "banana")
        )

        val hasPermission = authorizationService.hasPermission(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view,
                testEntity
            )
        )

        assertTrue(hasPermission)
    }

    @Test
    @WithMockUser(authorities = ["FIELD_CONTAINS_ROLE"])
    fun `should not have permission for entity where field list does not contain value in permission`() {
        val testEntity = TestEntity(
            name = "henk",
            fruits = mutableListOf("peer", "banana")
        )

        val hasPermission = authorizationService.hasPermission(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                view,
                testEntity
            )
        )

        assertFalse(hasPermission)
    }
}