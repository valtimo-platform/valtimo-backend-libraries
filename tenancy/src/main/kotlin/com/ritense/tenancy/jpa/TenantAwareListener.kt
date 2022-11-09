package com.ritense.tenancy.jpa;

import com.ritense.tenancy.TenantAware
import com.ritense.tenancy.TenantResolver
import java.util.function.Consumer
import javax.persistence.EntityNotFoundException
import javax.persistence.PrePersist
import javax.persistence.PreRemove
import javax.persistence.PreUpdate

class TenantAwareListener {

    @PreUpdate
    @PrePersist
    fun setTenant(entity: Any) {
        ifTenantEntity(entity) { item: TenantAware ->
            if (item.tenantId == null) {
                val tenantId = TenantResolver.getTenantId()
                requireNotNull(tenantId) {
                    "Tenant id missing"
                }
                item.tenantId = tenantId
            }
        }
    }

    @PreRemove
    fun preRemove(entity: Any) {
        ifTenantEntity(entity) { item: TenantAware ->
            val tenantId = TenantResolver.getTenantId()
            requireNotNull(tenantId) {
                "Tenant id missing"
            }
            if (tenantId != item.tenantId) {
                throw EntityNotFoundException()
            }
        }
    }

    private fun ifTenantEntity(entity: Any, callable: Consumer<TenantAware>) {
        if (entity is TenantAware) {
            callable.accept(entity)
        }
    }

}