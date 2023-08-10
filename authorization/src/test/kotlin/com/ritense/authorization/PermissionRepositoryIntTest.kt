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
import com.ritense.authorization.permission.condition.PermissionConditionOperator.EQUAL_TO
import com.ritense.authorization.permission.condition.PermissionConditionType.FIELD
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.authorization.testimpl.TestDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class PermissionRepositoryIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    lateinit var role: Role

    @BeforeEach
    fun setup() {
        role = Role(key = "test-role")
        roleRepository.saveAndFlush(role)
    }

    @Test
    fun `should save Permission`() {
        val permission = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = Action<TestDocument>(Action.VIEW_LIST),
            conditionContainer = ConditionContainer(listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    operator = EQUAL_TO,
                    value = "loan"
                ))
            ),
            role = role
        )

        permissionRepository.saveAndFlush(permission)

        val permissions = permissionRepository.findAllByResourceTypeAndAction(permission.resourceType, permission.action)
        assertThat(permissions).hasSize(1)
        assertThat(permissions[0].id).isNotNull
        assertThat(permissions[0].resourceType).isEqualTo(Class.forName("com.ritense.authorization.testimpl.TestDocument"))
        assertThat(permissions[0].action).isEqualTo(Action<TestDocument>(Action.VIEW_LIST))
        assertThat(permissions[0].conditionContainer.conditions).hasSize(1)
        assertThat(permissions[0].conditionContainer.conditions[0].type).isEqualTo(FIELD)
        assertTrue(permissions[0].conditionContainer.conditions[0] is FieldPermissionCondition<*>)
        assertThat((permissions[0].conditionContainer.conditions[0] as FieldPermissionCondition<*>).field).isEqualTo("document.name")
        assertThat((permissions[0].conditionContainer.conditions[0] as FieldPermissionCondition<*>).operator).isEqualTo(EQUAL_TO)
        assertThat((permissions[0].conditionContainer.conditions[0] as FieldPermissionCondition<*>).value).isEqualTo("loan")
    }

    @Test
    fun `should retrieve all Permissions for role key`() {
        val role2 = roleRepository.saveAndFlush(Role(key = "test-role2"))

        val permission = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = Action<TestDocument>(Action.VIEW_LIST),
            conditionContainer = ConditionContainer(listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    operator = EQUAL_TO,
                    value = "loan"
                ))
            ),
            role = role
        )

        val permission2 = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = Action<TestDocument>(Action.VIEW),
            conditionContainer = ConditionContainer(listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    operator = EQUAL_TO,
                    value = "loan"
                ))
            ),
            role = role
        )

        val permission3 = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = Action<TestDocument>(Action.ASSIGN),
            conditionContainer = ConditionContainer(listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    operator = EQUAL_TO,
                    value = "loan"
                ))
            ),
            role = role2
        )

        permissionRepository.saveAllAndFlush(listOf(permission, permission2, permission3))

        val permissions = permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf(role.key))

        assertThat(permissions).hasSize(2)

        assertThat(permissions[0].id).isNotNull
        assertThat(permissions[0].resourceType).isEqualTo(Class.forName("com.ritense.authorization.testimpl.TestDocument"))
        assertThat(permissions[0].action).isEqualTo(Action<TestDocument>(Action.VIEW_LIST))
        assertThat(permissions[0].conditionContainer.conditions).hasSize(1)
        assertThat(permissions[0].conditionContainer.conditions[0].type).isEqualTo(FIELD)
        assertTrue(permissions[0].conditionContainer.conditions[0] is FieldPermissionCondition<*>)
        assertThat((permissions[0].conditionContainer.conditions[0] as FieldPermissionCondition<*>).field).isEqualTo("document.name")
        assertThat((permissions[0].conditionContainer.conditions[0] as FieldPermissionCondition<*>).operator).isEqualTo(EQUAL_TO)
        assertThat((permissions[0].conditionContainer.conditions[0] as FieldPermissionCondition<*>).value).isEqualTo("loan")

        assertThat(permissions[1].id).isNotNull
        assertThat(permissions[1].resourceType).isEqualTo(Class.forName("com.ritense.authorization.testimpl.TestDocument"))
        assertThat(permissions[1].action).isEqualTo(Action<TestDocument>(Action.VIEW))
        assertThat(permissions[1].conditionContainer.conditions).hasSize(1)
        assertThat(permissions[1].conditionContainer.conditions[0].type).isEqualTo(FIELD)
        assertTrue(permissions[1].conditionContainer.conditions[0] is FieldPermissionCondition<*>)
        assertThat((permissions[1].conditionContainer.conditions[0] as FieldPermissionCondition<*>).field).isEqualTo("document.name")
        assertThat((permissions[1].conditionContainer.conditions[0] as FieldPermissionCondition<*>).operator).isEqualTo(EQUAL_TO)
        assertThat((permissions[1].conditionContainer.conditions[0] as FieldPermissionCondition<*>).value).isEqualTo("loan")
    }
}