package com.ritense.tenancy;

import com.ritense.tenancy.web.DelegatingTenantAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

object TenantResolver {

    /**
     * Resolve tenant-id from auth
     * @return Tenant id as string
     */
    fun getTenantId(): String? {
        return (SecurityContextHolder.getContext().authentication as? DelegatingTenantAuthenticationToken)?.tenantId
    }

}