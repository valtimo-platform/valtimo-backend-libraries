package com.ritense.tenancy;

import com.ritense.tenancy.config.TenantSpringContextHelper
import com.ritense.tenancy.web.DelegatingTenantAuthenticationToken
import com.ritense.valtimo.contract.config.ValtimoProperties
import org.springframework.security.core.context.SecurityContextHolder

object TenantResolver {

    const val DEFAULT_TENANT_ID = ""

    /**
     * Resolve tenant-id from auth
     * @return Tenant id as string
     */
    @JvmStatic
    fun getTenantId(valtimoProperties: ValtimoProperties = TenantSpringContextHelper.getValtimoProperties()): String {
        return if (valtimoProperties.app.enableTenancy != true) {
            DEFAULT_TENANT_ID
        } else {
            (SecurityContextHolder.getContext().authentication as DelegatingTenantAuthenticationToken).tenantId
        }
    }

}