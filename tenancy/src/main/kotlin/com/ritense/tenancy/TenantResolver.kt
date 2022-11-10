package com.ritense.tenancy;

import com.ritense.tenancy.web.DelegatingTenantAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

object TenantResolver {

    const val DEFAULT_TENANT_ID = ""

    /**
     * Resolve tenant-id from auth
     * @return Tenant id as string
     */
    @JvmStatic
    fun getTenantId() =
        (SecurityContextHolder
            .getContext()
            .authentication as? DelegatingTenantAuthenticationToken)?.tenantId ?: DEFAULT_TENANT_ID

}