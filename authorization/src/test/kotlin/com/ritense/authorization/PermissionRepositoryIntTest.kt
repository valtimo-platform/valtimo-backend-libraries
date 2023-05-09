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

import com.ritense.authorization.permission.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.Action.ASSIGN
import com.ritense.authorization.Action.LIST_VIEW
import com.ritense.authorization.Action.VIEW
import com.ritense.authorization.permission.PermissionConditionType.FIELD
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class PermissionRepositoryIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    lateinit var role: Role

    @BeforeEach
    fun setup() {
        role = Role("test-role")
        roleRepository.saveAndFlush(role)
    }

    @AfterEach
    fun cleanUp() {
        permissionRepository.deleteAll()
        roleRepository.deleteAll()
    }

    @Test
    fun `should save Permission`() {
        val permission = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = LIST_VIEW,
            conditions = listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    value = "loan"
                )
            ),
            roleKey = role.key
        )

        permissionRepository.saveAndFlush(permission)

        val permissions = permissionRepository.findAll()
        assertThat(permissions).hasSize(1)
        assertThat(permissions[0].id).isNotNull
        assertThat(permissions[0].resourceType).isEqualTo(Class.forName("com.ritense.authorization.testimpl.TestDocument"))
        assertThat(permissions[0].action).isEqualTo(LIST_VIEW)
        assertThat(permissions[0].conditions).hasSize(1)
        assertThat(permissions[0].conditions[0].type).isEqualTo(FIELD)
        assertTrue(permissions[0].conditions[0] is FieldPermissionCondition)
        assertThat((permissions[0].conditions[0] as FieldPermissionCondition).field).isEqualTo("document.name")
        assertThat((permissions[0].conditions[0] as FieldPermissionCondition).value).isEqualTo("loan")
    }

    @Test
    fun `should retrireve all Permissions for role key`() {
        val role2 = roleRepository.saveAndFlush(Role("test-role2"))

        val permission = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = LIST_VIEW,
            conditions = listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    value = "loan"
                )
            ),
            roleKey = role.key
        )

        val permission2 = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = VIEW,
            conditions = listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    value = "loan"
                )
            ),
            roleKey = role.key
        )

        val permission3 = Permission(
            resourceType = Class.forName("com.ritense.authorization.testimpl.TestDocument"),
            action = ASSIGN,
            conditions = listOf(
                FieldPermissionCondition(
                    field = "document.name",
                    value = "loan"
                )
            ),
            roleKey = role2.key
        )

        permissionRepository.saveAllAndFlush(listOf(permission, permission2, permission3))

        val permissions = permissionRepository.findAllByRoleKeyIn(listOf(role.key))

        assertThat(permissions).hasSize(2)

        assertThat(permissions[0].id).isNotNull
        assertThat(permissions[0].resourceType).isEqualTo(Class.forName("com.ritense.authorization.testimpl.TestDocument"))
        assertThat(permissions[0].action).isEqualTo(LIST_VIEW)
        assertThat(permissions[0].conditions).hasSize(1)
        assertThat(permissions[0].conditions[0].type).isEqualTo(FIELD)
        assertTrue(permissions[0].conditions[0] is FieldPermissionCondition)
        assertThat((permissions[0].conditions[0] as FieldPermissionCondition).field).isEqualTo("document.name")
        assertThat((permissions[0].conditions[0] as FieldPermissionCondition).value).isEqualTo("loan")

        assertThat(permissions[1].id).isNotNull
        assertThat(permissions[1].resourceType).isEqualTo(Class.forName("com.ritense.authorization.testimpl.TestDocument"))
        assertThat(permissions[1].action).isEqualTo(VIEW)
        assertThat(permissions[1].conditions).hasSize(1)
        assertThat(permissions[1].conditions[0].type).isEqualTo(FIELD)
        assertTrue(permissions[1].conditions[0] is FieldPermissionCondition)
        assertThat((permissions[1].conditions[0] as FieldPermissionCondition).field).isEqualTo("document.name")
        assertThat((permissions[1].conditions[0] as FieldPermissionCondition).value).isEqualTo("loan")
    }
}