/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.authorization.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

class RoleDeployer(
    private val objectMapper: ObjectMapper,
    private val roleRepository: RoleRepository,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
) : ChangesetDeployer {

    override fun getPath() = "classpath*:**/*.role.json"

    override fun before() {
        if (clearTables) {
            roleRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<RoleChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.roles,
                key = KEY,
                deploy = { deploy(changeset.roles) }
            )
        )
    }

    fun deploy(roles: List<String>) {
        roleRepository.saveAll(roles.map { Role(key = it) })
    }

    companion object {
        const val KEY = "role"
    }
}