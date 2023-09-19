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
import com.ritense.authorization.permission.condition.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.condition.PermissionConditionOperator
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.authorization.testimpl.TestEntity
import com.ritense.authorization.testimpl.TestEntityActionProvider
import java.util.UUID
import javax.transaction.Transactional
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser

@Transactional
class AuthorizationServiceIntTest @Autowired constructor(
    private val authorizationService: AuthorizationService
) : BaseIntegrationTest() {

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @BeforeEach
    fun beforeEach() {
        roleRepository.deleteByKeyIn(listOf("test-role"))
        roleRepository.save(Role(key = "test-role"))
    }

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
    @WithMockUser(authorities = ["test-role"])
    fun `should pass permission check when entity is not null`() {

        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(emptyList()),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        assertDoesNotThrow {
            requirePermission(
                entity = TestEntity()
            )
        }
    }

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `should fail permission check when entity is null`() {

        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(emptyList()),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        assertThrows<AccessDeniedException> {
            requirePermission()
        }
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

    @Test
    fun `should find all roles that pass an authorization check`() {
        val entity = TestEntity()
        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(
                    listOf(
                        FieldPermissionCondition(
                            field = "name",
                            operator = PermissionConditionOperator.EQUAL_TO,
                            value = "fail"
                        )
                    )
                ),
                role
            ),
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(
                    listOf(
                        FieldPermissionCondition(
                            field = "name",
                            operator = PermissionConditionOperator.EQUAL_TO,
                            value = entity.name
                        )
                    )
                ),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val authorizedRoles = authorizationService.getAuthorizedRoles(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                action = Action(Action.VIEW),
                TestEntity()
            )
        )

        assertThat(authorizedRoles, hasItems(role))
    }

    @Test
    fun `should not find roles that fail an authorization check`() {
        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(
                    listOf(
                        FieldPermissionCondition(
                            field = "name",
                            operator = PermissionConditionOperator.EQUAL_TO,
                            "fail"
                        )
                    )
                ),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val authorizedRoles = authorizationService.getAuthorizedRoles(
            EntityAuthorizationRequest(
                TestEntity::class.java,
                action = Action(Action.VIEW),
                TestEntity()
            )
        )

        assertThat(authorizedRoles, `is`(empty()))
    }

    fun requirePermission(
        action: Action<TestEntity> = Action(Action.VIEW),
        entity: TestEntity? = null
    ) {
        if (entity == null) {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(TestEntity::class.java, action)
            )
        } else {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(TestEntity::class.java, action, entity)
            )
        }
    }

}
