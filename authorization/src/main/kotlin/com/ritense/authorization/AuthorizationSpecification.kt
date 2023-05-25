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

package com.ritense.authorization

import com.ritense.authorization.permission.Permission
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

abstract class AuthorizationSpecification<T : Any>(
    protected val authContext: AuthorizationRequest<T>,
    protected val permissions: List<Permission>
) : Specification<T> {

    internal open fun isAuthorized(entity: T): Boolean {
        return permissions.filter { permission ->
            entity::class.java == permission.resourceType && authContext.action == permission.action
        }.any { permission ->
            permission.appliesTo(authContext.resourceType, entity)
        }
    }

    fun combinePredicates(criteriaBuilder: CriteriaBuilder, predicates: List<Predicate>): Predicate {
        return criteriaBuilder.or(*predicates.toTypedArray())
    }

    abstract override fun toPredicate(
        root: Root<T>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate
}