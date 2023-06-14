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

package com.ritense.authorization.deployment

import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.Action
import com.ritense.authorization.PermissionRepository
import com.ritense.authorization.permission.ContainerPermissionCondition
import com.ritense.authorization.permission.ExpressionPermissionCondition
import com.ritense.authorization.permission.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionConditionOperator
import com.ritense.authorization.testimpl.TestDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class PermissionDeployerTest {

    lateinit var permissionDeployer: PermissionDeployer
    lateinit var permissionRepository: PermissionRepository

    @BeforeEach
    fun beforeEach() {
        val mapper = jacksonObjectMapper()
        mapper.registerSubtypes(
            NamedType(ContainerPermissionCondition::class.java),
            NamedType(ExpressionPermissionCondition::class.java),
            NamedType(FieldPermissionCondition::class.java)
        )
        permissionRepository = mock()
        permissionDeployer = PermissionDeployer(
            mapper,
            permissionRepository
        )
    }

    @Test
    fun `should deploy permissions from configuration file`() {
        permissionDeployer.deploy(
            """
            {
                "changesetId": "test-document-v1",
                "permissions": [
                    {
                        "resourceType": "com.ritense.authorization.testimpl.TestDocument",
                        "action": "list_view",
                        "roleKey": "ROLE_USER",
                        "conditionContainer": {
                            "conditions": [
                                {
                                    "type": "field",
                                    "field": "document.name",
                                    "operator": "==",
                                    "value": "loan"
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()
        )

        val captor = argumentCaptor<Permission>()
        verify(permissionRepository, times(1)).save(captor.capture())
        assertThat(captor.firstValue.id).isNotNull()
        assertThat(captor.firstValue.resourceType).isEqualTo(TestDocument::class.java)
        assertThat(captor.firstValue.action).isEqualTo(Action<Any>(Action.LIST_VIEW))
        assertThat(captor.firstValue.roleKey).isEqualTo("ROLE_USER")
        assertThat(captor.firstValue.conditionContainer.conditions).hasSize(1)
        assertTrue(captor.firstValue.conditionContainer.conditions[0] is FieldPermissionCondition<*>)
        val condition = captor.firstValue.conditionContainer.conditions[0] as FieldPermissionCondition<*>
        assertThat(condition.field).isEqualTo("document.name")
        assertThat(condition.operator).isEqualTo(PermissionConditionOperator.EQUAL_TO)
        assertThat(condition.value).isEqualTo("loan")
    }
}