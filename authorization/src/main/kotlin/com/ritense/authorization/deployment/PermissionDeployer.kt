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
import com.ritense.authorization.PermissionRepository
import com.ritense.valtimo.contract.importchangelog.ChangesetDeployer
import com.ritense.valtimo.contract.importchangelog.ChangesetDetails

class PermissionDeployer(
    private val objectMapper: ObjectMapper,
    private val permissionRepository: PermissionRepository,
) : ChangesetDeployer {

    override fun getPath() = "classpath*:**/*.permission.json"

    override fun getChangesetDetails(filename: String, content: String): ChangesetDetails {
        val permissionChangeset = objectMapper.readValue<PermissionChangeset>(content)
        return ChangesetDetails(
            permissionChangeset.changesetId,
            permissionChangeset.permissions
        )
    }

    override fun deploy(content: String) {
        val permissionChangeset = objectMapper.readValue<PermissionChangeset>(content)
        permissionChangeset.permissions
            .map { it.toPermission() }
            .forEach { permission ->
                permissionRepository.deleteAllByResourceTypeAndActionAndRoleKey(
                    permission.resourceType,
                    permission.action,
                    permission.roleKey
                )
                permissionRepository.save(permission)
            }
    }
}