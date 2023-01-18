/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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