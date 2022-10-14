/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.authentication

import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria
import com.ritense.valtimo.contract.authentication.util.FakeValtimoUserUtil
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

class FakeUserManagementService : UserManagementService {
    override fun createUser(user: ManageableUser): ManageableUser {
        logger.info { "createUser ${user.email}" }
        return user
    }

    override fun updateUser(user: ManageableUser): ManageableUser {
        logger.info { "updateUser ${user.email}" }
        return user
    }

    override fun deleteUser(userId: String) {
        logger.info { "deleteUser $userId" }
    }

    override fun resendVerificationEmail(userId: String): Boolean {
        logger.info { "resendVerificationEmail $userId" }
        return true
    }

    override fun activateUser(userId: String) {
        logger.info { "activateUser $userId" }
    }

    override fun deactivateUser(userId: String) {
        logger.info { "deactivateUser $userId" }
    }

    override fun getAllUsers(pageable: Pageable): Page<ManageableUser> {
        logger.info { "getAllUsers ${pageable.pageSize}" }
        return Page.empty()
    }

    override fun getAllUsers(): MutableList<ManageableUser> {
        logger.info { "getAllUsers" }
        return mutableListOf()
    }

    override fun queryUsers(searchTerm: String, pageable: Pageable): Page<ManageableUser> {
        logger.info { "deactivateUser $searchTerm" }
        return Page.empty()
    }

    override fun findByEmail(email: String): Optional<ManageableUser> {
        return Optional.of(FakeValtimoUserUtil.valtimoUserBuilder().email(email).build())
    }

    override fun findById(userId: String): ManageableUser? {
        logger.info { "findById $userId" }
        return FakeValtimoUserUtil.valtimoUserBuilder().id(userId).build()
    }

    override fun findByRole(authority: String): MutableList<ManageableUser> {
        logger.info { "findByRole $authority" }
        return mutableListOf()
    }

    override fun findByRoles(groupsCriteria: SearchByUserGroupsCriteria): MutableList<ManageableUser> {
        logger.info { "findByRoles $groupsCriteria" }
        return mutableListOf()
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
