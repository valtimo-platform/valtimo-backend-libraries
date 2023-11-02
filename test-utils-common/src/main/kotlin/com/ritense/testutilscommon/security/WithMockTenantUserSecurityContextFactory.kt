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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.testutilscommon.security

import com.ritense.tenancy.authentication.TenantAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockTenantUserSecurityContextFactory : WithSecurityContextFactory<WithMockTenantUser> {

    override fun createSecurityContext(customUser: WithMockTenantUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val authorities = customUser.roles.map { SimpleGrantedAuthority(it) }.toList()
        val principal = User(customUser.username, "", authorities)
        val auth: Authentication =
            TenantAuthenticationToken(
                principal = principal,
                credentials = "",
                authorities = principal.authorities.toList(),
                tenantId = "1",
                fullName = customUser.fullName
            )
        context.authentication = auth
        return context
    }
}