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
import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import com.ritense.valtimo.contract.importchangelog.ChangesetDeployer
import com.ritense.valtimo.contract.importchangelog.ChangesetDetails

class RoleDeployer(
    private val objectMapper: ObjectMapper,
    private val roleRepository: RoleRepository
) : ChangesetDeployer {

    override fun getPath() = "classpath*:**/*.role.json"

    override fun getChangesetDetails(filename: String, content: String): ChangesetDetails {
        val changeset = objectMapper.readValue<RoleChangeset>(content)
        return ChangesetDetails(
            changeset.changesetId,
            changeset.roles
        )
    }

    override fun deploy(content: String) {
        val changeset = objectMapper.readValue<RoleChangeset>(content)
        roleRepository.saveAll(changeset.roles.map { Role(it) })
    }
}