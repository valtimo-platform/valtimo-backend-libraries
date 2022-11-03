package com.ritense.tenancy.jpa;

import com.ritense.tenancy.TenantAware
import com.ritense.tenancy.TenantResolver
import java.util.function.Consumer
import javax.inject.Inject
import javax.persistence.EntityNotFoundException
import javax.persistence.PrePersist
import javax.persistence.PreRemove
import javax.persistence.PreUpdate

class TenantAwareListener {

    @Inject
    lateinit var tenantResolver: TenantResolver

    @PreUpdate
    @PrePersist
    fun setTenant(entity: Any) {
        ifTenantEntity(entity) { item: TenantAware ->
            item.tenantId = tenantResolver.getTenantId()
        }
    }

    @PreRemove
    fun preRemove(entity: Any) {
        ifTenantEntity(entity) { item: TenantAware ->
            if (tenantResolver.getTenantId() != item.tenantId) {
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