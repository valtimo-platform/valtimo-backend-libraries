/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.authorization.permission

import com.ritense.authorization.Action
import com.ritense.authorization.NoContext
import com.ritense.authorization.criteriabuilder.AbstractQueryWrapper
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.role.Role
import com.ritense.valtimo.contract.database.QueryDialectHelper
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.hibernate.annotations.Type
import java.util.UUID

@Entity
@Table(name = "permission")
data class Permission(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "resource_type")
    val resourceType: Class<*>,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "permission_actions",
        joinColumns = [JoinColumn(name = "permission_id")]
    )
    @field:jakarta.validation.constraints.Size(min = 1)
    val actions: MutableList<Action<*>> = mutableListOf(),

    @Type(value = JsonType::class)
    @Column(name = "conditions", columnDefinition = "json")
    val conditionContainer: ConditionContainer,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    val role: Role,

    @Column(name = "context_resource_type")
    val contextResourceType: Class<*>? = null,

    @Type(value = JsonType::class)
    @Column(name = "context_conditions", columnDefinition = "json")
    val contextConditionContainer: ConditionContainer? = null,
) {
    @Deprecated("Since 12.2.0")
    constructor(
        id: UUID,
        resourceType: Class<*>,
        action: Action<*>,
        conditionContainer: ConditionContainer,
        role: Role
    ) : this(id, resourceType, mutableListOf(action), conditionContainer, role, null, null) {

    }

    init {
        require(
            (
                (contextResourceType != null && contextConditionContainer != null)
                    || (contextResourceType == null
                    && (contextConditionContainer == null || contextConditionContainer.conditions.isEmpty())
                    )
                )
        )
    }

    @Deprecated("Since 12.2.0")
    fun <T> appliesTo(
        resourceType: Class<T>,
        entity: Any?,
    ): Boolean = appliesTo(resourceType, entity, null, null)

    fun <T> appliesTo(
        resourceType: Class<T>,
        entity: Any?,
        contextResourceType: Class<*>? = null,
        contextEntity: Any? = null,
    ): Boolean {
        return appliesInContext(contextResourceType, contextEntity)
            && if (this.resourceType == resourceType) {
            if (entity == null && conditionContainer.conditions.isNotEmpty()) {
                return false
            }
            conditionContainer.conditions
                .all { it.isValid(entity!!) }
        } else {
            false
        }
    }

    @Deprecated("Since 12.2.0")
    fun <T : Any> toPredicate(
        root: Root<T>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper,
    ): Predicate {
        val requestContextResourceType: Class<*>? = null
        val requestContextEntity: Any? = null

        require(
            appliesInContext(requestContextResourceType, requestContextEntity)
        )

        val customQuery = AbstractQueryWrapper(query)
        return criteriaBuilder
            .and(
                *conditionContainer.conditions.map {
                    it.toPredicate(
                        root,
                        customQuery,
                        criteriaBuilder,
                        resourceType,
                        queryDialectHelper
                    )
                }.toTypedArray()
            )
    }

    fun <T : Any> toPredicate(
        root: Root<T>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        request: AuthorizationRequest<T>,
        queryDialectHelper: QueryDialectHelper,
    ): Predicate {
        var requestContextResourceType: Class<*>? = null
        var requestContextEntity: Any? = null

        if (request is EntityAuthorizationRequest) {
            requestContextResourceType = request.context?.resourceType
            requestContextEntity = request.context?.entity
        }

        require(
            appliesInContext(requestContextResourceType, requestContextEntity)
        )

        val customQuery = AbstractQueryWrapper(query)
        return criteriaBuilder
            .and(
                *conditionContainer.conditions.map {
                    it.toPredicate(
                        root,
                        customQuery,
                        criteriaBuilder,
                        request.resourceType,
                        queryDialectHelper
                    )
                }.toTypedArray()
            )
    }

    fun <U> appliesInContext(
        contextResourceType: Class<U>?,
        contextEntity: Any?
    ): Boolean {
        return this.contextResourceType == null
            || (this.contextResourceType == NoContext::class.java && contextResourceType == null)
            || (contextResourceType == this.contextResourceType
            && contextConditionContainer?.let { container ->
            container.conditions
                .all { it.isValid(contextEntity!!) }
        } ?: false)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Permission

        if (resourceType != other.resourceType) return false
        if (actions.toList() != other.actions.toList()) return false
        if (conditionContainer != other.conditionContainer) return false
        if (role != other.role) return false
        if (contextResourceType != other.contextResourceType) return false
        if (contextConditionContainer != other.contextConditionContainer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resourceType.hashCode()
        result = 31 * result + actions.hashCode()
        result = 31 * result + conditionContainer.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + (contextResourceType?.hashCode() ?: 0)
        result = 31 * result + (contextConditionContainer?.hashCode() ?: 0)
        return result
    }
}