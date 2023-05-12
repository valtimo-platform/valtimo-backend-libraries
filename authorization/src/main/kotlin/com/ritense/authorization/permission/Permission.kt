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
import com.ritense.valtimo.contract.database.QueryDialectHelper
import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Entity
@Table(name = "permission")
data class Permission(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "resource_type")
    val resourceType: Class<*>,

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    val action: Action,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "conditions", columnDefinition = "json")
    val conditionContainer: ConditionContainer,

    @Column(name = "role_key", nullable = false)
    val roleKey: String,
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
        query: CriteriaQuery<*>,
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
                        queryDialectHelper)
                }.toTypedArray()
            )
    }
}