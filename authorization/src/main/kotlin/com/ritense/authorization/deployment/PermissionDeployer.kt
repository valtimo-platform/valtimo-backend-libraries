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
import com.ritense.authorization.AuthorizationSupportedHelper
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.RoleRepository
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

class PermissionDeployer(
    private val objectMapper: ObjectMapper,
    private val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
) : ChangesetDeployer {

    override fun getPath() = "classpath*:**/*.permission.json"

    override fun before() {
        if (clearTables) {
            permissionRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<PermissionChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.permissions,
                key = KEY,
                deploy = { deploy(changeset.permissions) }
            )
        )
    }

    fun deploy(permissions: List<PermissionDto>) {
        val permissionsToSave = permissions.map {
            AuthorizationSupportedHelper.checkSupported(it.resourceType)
            it.toPermission(roleRepository)
        }

        permissionRepository.saveAll(permissionsToSave)
    }

    companion object {
        const val KEY = "permission"
    }
}