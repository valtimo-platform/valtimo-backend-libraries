package com.ritense.tenancy.web;

import com.ritense.tenancy.TenantResolver
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class TenantAuthorizationFilter : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val curAuthentication = SecurityContextHolder.getContext().authentication
        if (curAuthentication != null) {
            SecurityContextHolder.getContext().authentication = getAuthenticationToken(curAuthentication)
        }
        filterChain.doFilter(request, response)
    }

    private fun getAuthenticationToken(authentication: Authentication): DelegatingTenantAuthenticationToken {
        val tenantId = TenantResolver.DEFAULT_TENANT_ID
        // TODO: get auth tenant from keycloak properties/attribute/realm
        return DelegatingTenantAuthenticationToken(authentication, tenantId)
    }
}