/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.outbox

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.util.stream.Collectors

open class UserProvider {
    open fun getCurrentUserLogin(): String? {
        val securityContext = SecurityContextHolder.getContext()
        val authentication = securityContext.authentication
        var userName: String? = null
        if (authentication != null) {
            userName = authentication.name
        }
        return userName
    }

    open fun getCurrentUserRoles(): List<String> {
        val roles: List<String> = ArrayList()
        val securityContext = SecurityContextHolder.getContext()
        val authentication = securityContext.authentication
        return if (authentication != null) {
            authentication
                .authorities
                .stream()
                .map { obj: GrantedAuthority -> obj.authority }
                .collect(Collectors.toList())
        } else roles
    }
}