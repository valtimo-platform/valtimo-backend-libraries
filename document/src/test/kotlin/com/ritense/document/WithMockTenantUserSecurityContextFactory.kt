package com.ritense.document

import com.ritense.tenancy.authentication.TenantAuthenticationToken
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockTenantUserSecurityContextFactory : WithSecurityContextFactory<WithMockTenantUser> {

    override fun createSecurityContext(customUser: WithMockTenantUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(
            SimpleGrantedAuthority(USER),
            SimpleGrantedAuthority(ADMIN)
        )
        val principal = User(customUser.username, "", authorities)
        val auth: Authentication =
            TenantAuthenticationToken(
                principal = principal,
                credentials = "",
                authorities = principal.authorities.toList(),
                tenantId = "1",
                fullName =  customUser.fullName
            )
        context.authentication = auth
        return context
    }
}