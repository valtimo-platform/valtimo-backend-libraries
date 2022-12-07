package com.ritense.tenancy;

import com.ritense.tenancy.authentication.TenantAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

object TenantResolver {

    /**
     * Resolve tenant-id from auth
     * @return Tenant id as string not empty
     */
    @JvmStatic
    fun getTenantId(): String {
        val tenantId = (SecurityContextHolder.getContext().authentication as TenantAuthenticationToken).tenantId
        require(tenantId.isNotEmpty()) { "'tenantId' can not be empty" }
        return tenantId
    }

}