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

import com.ritense.valtimo.contract.authentication.model.Profile
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import com.ritense.valtimo.contract.authentication.util.FakeValtimoUserUtil
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

class FakeUserRepository : CurrentUserRepository {

    override fun getCurrentUser(currentUserLogin: String): ValtimoUser {
        return FakeValtimoUserUtil.valtimoUserBuilder().email(currentUserLogin).build()
    }

    override fun changePassword(currentUserLogin: String, newPassword: String) {
        logger.info { "changePassword $currentUserLogin" }
    }

    override fun updateProfile(currentUserLogin: String, profile: Profile) {
        logger.info { "updateProfile $currentUserLogin" }
    }

    override fun supports(authentication: Class<out Authentication>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
