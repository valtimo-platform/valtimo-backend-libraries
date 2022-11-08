package com.ritense.tenancy.web;

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import javax.security.auth.Subject

class DelegatingTenantAuthenticationToken(
    private val delegate: Authentication,
    var tenantId: String
) : Authentication {

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

    @Throws(IllegalArgumentException::class)
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
