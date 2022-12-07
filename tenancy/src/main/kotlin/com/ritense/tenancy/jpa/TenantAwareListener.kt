package com.ritense.tenancy.jpa;

import com.ritense.tenancy.TenantResolver
import javax.persistence.EntityNotFoundException
import javax.persistence.PrePersist
import javax.persistence.PreRemove
import javax.persistence.PreUpdate

class TenantAwareListener {

    @PreUpdate
    @PrePersist
    fun setTenant(entity: Any) {
        if (entity is TenantAware) {
            entity.tenantId = TenantResolver.getTenantId()
        }
    }

    @PreRemove
    fun preRemove(entity: Any) {
        if (entity is TenantAware) {
            val tenantId = TenantResolver.getTenantId()
            if (tenantId != entity.tenantId) {
                throw EntityNotFoundException("Tenant mismatch")
            }
        }
    }

}