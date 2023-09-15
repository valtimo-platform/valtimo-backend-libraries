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
import com.ritense.authorization.testimpl.TestEntityActionProvider.Companion.view_list
import com.ritense.authorization.testimpl.TestEntityRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

class ContainsIntTest() : BaseIntegrationTest() {

    @Autowired
    lateinit var repository: TestEntityRepository

    @Autowired
    lateinit var authorizationService: AuthorizationService

    @Test
    @WithMockUser(authorities = ["TEST_ROLE"])
    fun `should find entity where json array contains value in permission`(){
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

        //TODO: assert 1 result found
        testEntity.name

    }

    @Test
    @WithMockUser(authorities = ["TEST_ROLE"])
    fun `should not find entity where json array does not contain value in permission`(){
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

        //TODO: assert no results found
        testEntity.name
    }
}