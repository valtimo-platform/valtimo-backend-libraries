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

package com.ritense.valtimo.contract.authentication.util

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder

object FakeValtimoUserUtil {

    fun valtimoUserBuilder(): ValtimoUserBuilder {
        return ValtimoUserBuilder()
            .id("anId")
            .username("aUserName")
            .name("aName")
            .email("anEmail")
            .firstName("aFirstName")
            .lastName("aLastName")
            .phoneNo("aPhoneNumber")
            .isEmailVerified(true)
            .langKey("NL")
            .blocked(false)
            .activated(true)
            .roles(listOf(AuthoritiesConstants.USER))
    }
}
