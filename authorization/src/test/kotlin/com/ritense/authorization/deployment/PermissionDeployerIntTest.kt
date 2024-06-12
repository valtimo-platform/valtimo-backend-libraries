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

package com.ritense.authorization.deployment

import com.ritense.authorization.Action
import com.ritense.authorization.Action.Companion.VIEW
import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.permission.condition.FieldPermissionCondition
import com.ritense.authorization.permission.condition.PermissionConditionOperator
import com.ritense.authorization.testimpl.TestDocument
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class PermissionDeployerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var changesetRepository: ChangesetRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Test
    fun `should deploy permission changeset from resource folder`() {

        val changeset = changesetRepository.findById("testdocument-v1")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/testdocument.permission.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.parse("2023-06-13T00:00:00Z"), Instant.now())
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isEqualTo("620892c4c4f31e502c81dd237b807d77")
    }

    @Test
    fun `should deploy permission from resource folder`() {

        val permissions = permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("ROLE_USER"))

        assertThat(permissions).hasSize(1)
        assertThat(permissions[0].id).isNotNull()
        assertThat(permissions[0].resourceType).isEqualTo(TestDocument::class.java)
        assertThat(permissions[0].action).isEqualTo(Action<Any>(VIEW))
        assertThat(permissions[0].role.key).isEqualTo("ROLE_USER")
        assertThat(permissions[0].conditionContainer.conditions).hasSize(1)
        assertTrue(permissions[0].conditionContainer.conditions[0] is FieldPermissionCondition<*>)
        val condition = permissions[0].conditionContainer.conditions[0] as FieldPermissionCondition<*>
        assertThat(condition.field).isEqualTo("document.name")
        assertThat(condition.operator).isEqualTo(PermissionConditionOperator.IN)
        assertThat(condition.value).isEqualTo(listOf("loan", "gift"))
    }
}