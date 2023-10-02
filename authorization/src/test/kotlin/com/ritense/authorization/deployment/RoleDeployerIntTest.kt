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

import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.role.RoleRepository
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant

internal class RoleDeployerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var changesetRepository: ChangesetRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Test
    fun `should deploy role changeset from resource folder`() {

        val changeset = changesetRepository.findById("roles-v1")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/all.role.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.parse("2023-06-13T00:00:00Z"), Instant.now())
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).hasSize(32)
    }

    @Test
    fun `should deploy role from resource folder`() {

        val roles = roleRepository.findAll(PageRequest.of(0, 10, Sort.by("key"))).content

        assertThat(roles).hasSizeGreaterThan(3)
        assertThat(roles.map { it.key }).contains("ROLE_USER", "ROLE_ADMIN", "ROLE_UPDATE")
    }
}