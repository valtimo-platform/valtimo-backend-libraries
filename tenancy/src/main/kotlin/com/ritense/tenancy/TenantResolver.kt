package com.ritense.tenancy;

import com.ritense.tenancy.web.DelegatingTenantAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

object TenantResolver {

    private const val REQUEST_PARAM_KEY = "tenant"

    /**
     * Resolve tenant-id from auth, can be overridden with request param
     * @return Tenant id string
     */
    // resolve tenant-id to "no_tenant", unless "?tenant=my-tenant" is added to url which will use the value of "tenant"
    fun getTenantId(): String {
        // resolve from auth
        try {
            return (SecurityContextHolder.getContext().authentication as DelegatingTenantAuthenticationToken).tenantId
        } catch (t: Throwable) {
            throw RuntimeException("Failed to retrieve user tenant-id", t)
        }
    }

}