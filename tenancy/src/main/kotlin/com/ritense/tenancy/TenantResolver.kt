package com.ritense.tenancy;

import com.ritense.tenancy.config.TenantSpringContextHelper
import com.ritense.tenancy.web.DelegatingTenantAuthenticationToken
import com.ritense.valtimo.contract.config.ValtimoProperties
import org.springframework.security.core.context.SecurityContextHolder

/*
 This Class has some issues:
 - Fixme: During the startup phase of a Spring App the context can be null thus getTenantId is not able to
    getValtimoProperties. Solved now by providing this via arguments and having 1 overload version.
 - Fixme: SecurityContextHolder.getContext().authentication can be null. Like in above issue when the app is still
    starting.
 */
object TenantResolver {

    const val DEFAULT_TENANT_ID = ""

    /**
     * Resolve tenant-id from auth
     * @return Tenant id as string
     */
    @JvmStatic
    fun getTenantId(valtimoProperties: ValtimoProperties): String {
        return if (valtimoProperties.app.enableTenancy != true) {
            DEFAULT_TENANT_ID
        } else {
            (SecurityContextHolder
                .getContext()
                .authentication as? DelegatingTenantAuthenticationToken)?.tenantId ?: DEFAULT_TENANT_ID
        }
    }

    @JvmStatic
    fun getTenantId(): String {
        return getTenantId(TenantSpringContextHelper.getValtimoProperties())
    }

}