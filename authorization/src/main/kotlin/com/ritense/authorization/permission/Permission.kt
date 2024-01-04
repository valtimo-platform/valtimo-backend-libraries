/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.authorization.role.Role
import com.ritense.valtimo.contract.database.QueryDialectHelper
import io.hypersistence.utils.hibernate.type.json.JsonType
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Embedded
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

@Entity
@Table(name = "permission")
data class Permission(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "resource_type")
    val resourceType: Class<*>,

    @Column(name = "action")
    @Embedded
    val action: Action<*>,

    @Type(value = JsonType::class)
    @Column(name = "conditions", columnDefinition = "json")
    val conditionContainer: ConditionContainer,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    val role: Role,
) {
    fun <T> appliesTo(resourceType: Class<T>, entity: Any?): Boolean {
        return if (this.resourceType == resourceType) {
            if (entity == null && conditionContainer.conditions.isNotEmpty()) {
                return false
            }
            conditionContainer.conditions
                .map { it.isValid(entity!!) }
                .all { it }
        } else {
            false
        }
    }

    fun <T : Any> toPredicate(
        root: Root<T>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder,
        resourceType: Class<T>,
        queryDialectHelper: QueryDialectHelper
    ): Predicate {
        return criteriaBuilder
            .and(
                *conditionContainer.conditions.map {
                    it.toPredicate(
                        root,
                        query,
                        criteriaBuilder,
                        resourceType,
                        queryDialectHelper
                    )
                }.toTypedArray()
            )
    }
}