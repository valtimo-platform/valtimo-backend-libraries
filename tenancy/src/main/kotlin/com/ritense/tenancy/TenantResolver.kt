package com.ritense.tenancy;

import com.ritense.tenancy.authentication.TenantAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

object TenantResolver {

    /**
     * Resolve tenant-id from auth
     * @return Tenant id as string or null
     */
    @JvmStatic
    fun getTenantId(): String =
        (SecurityContextHolder.getContext().authentication as TenantAuthenticationToken).tenantId

}