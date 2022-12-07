package com.ritense.tenancy.jpa

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ritense.tenancy.jpa.AbstractTenantAwareAggregateRoot.Companion.TENANT_COLUMN
import com.ritense.tenancy.jpa.AbstractTenantAwareAggregateRoot.Companion.TENANT_FILTER_NAME
import com.ritense.tenancy.jpa.AbstractTenantAwareAggregateRoot.Companion.TENANT_PARAMETER_NAME
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef
import org.springframework.data.domain.AbstractAggregateRoot
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@FilterDef(
    name = TENANT_FILTER_NAME,
    defaultCondition = "$TENANT_COLUMN = :$TENANT_PARAMETER_NAME",
    parameters = [ParamDef(name = TENANT_PARAMETER_NAME, type = "string")]
)
@Filter(name = TENANT_FILTER_NAME)
@EntityListeners(TenantAwareListener::class)
abstract class AbstractTenantAwareAggregateRoot<T : AbstractTenantAwareAggregateRoot<T>?> : AbstractAggregateRoot<T>(),
    TenantAware
{

    @JsonIgnore
    @Column(name = TENANT_COLUMN, columnDefinition = "VARCHAR(256)", nullable = false)
    override var tenantId: String = "" // TenantAwareListener will populate value

    companion object {
        const val TENANT_FILTER_NAME = "tenantFilter"
        const val TENANT_PARAMETER_NAME = "tenantId"
        const val TENANT_COLUMN = "tenant_id"
    }

}