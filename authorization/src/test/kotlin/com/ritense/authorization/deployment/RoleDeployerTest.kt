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
 * See the License for the specific language governing roles and
 * limitations under the License.
 */

package com.ritense.authorization.deployment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class RoleDeployerTest {

    lateinit var roleDeployer: RoleDeployer
    lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun beforeEach() {
        roleRepository = mock()
        roleDeployer = RoleDeployer(
            jacksonObjectMapper(),
            roleRepository
        )
    }

    @Test
    fun `should deploy roles from configuration file`() {
        roleDeployer.deploy(
            """
            {
                "changesetId": "roles-v1",
                "roles": [
                    "ROLE_USER",
                    "ROLE_ADMIN"
                ]
            }
        """.trimIndent()
        )

        val captor = argumentCaptor<List<Role>>()
        verify(roleRepository, times(1)).saveAll(captor.capture())
        assertThat(captor.firstValue[0].key).isEqualTo("ROLE_USER")
        assertThat(captor.firstValue[1].key).isEqualTo("ROLE_ADMIN")
    }
}