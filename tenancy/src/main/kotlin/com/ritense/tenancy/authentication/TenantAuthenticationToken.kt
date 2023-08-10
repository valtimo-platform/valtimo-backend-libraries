/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.tenancy.authentication;

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import javax.security.auth.Subject

class TenantAuthenticationToken(
    private val delegate: Authentication,
    override val tenantId: String,
    val fullName: String
) : Authentication, TenantAware {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? {
        return delegate.authorities
    }

    override fun getCredentials(): Any {
        return delegate.credentials
    }

    override fun getDetails(): Any {
        return delegate.details
    }

    override fun getPrincipal(): Any {
        return delegate.principal
    }

    override fun isAuthenticated(): Boolean {
        return delegate.isAuthenticated
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        delegate.isAuthenticated = isAuthenticated
    }

    override fun getName(): String {
        return delegate.name
    }

    override fun implies(subject: Subject): Boolean {
        return delegate.implies(subject)
    }

}