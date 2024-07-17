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

package com.ritense.dashboard.repository

import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.dashboard.domain.Dashboard
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

class DashboardSpecification(
    authRequest: AuthorizationRequest<Dashboard>,
    permissions: List<Permission>,
    private val repository: DashboardRepository,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<Dashboard>(authRequest, permissions) {
    override fun identifierToEntity(identifier: String): Dashboard {
        return repository.findById(identifier)
            .orElseThrow { RuntimeException("No dashboard found with key '$identifier'") }
    }

    override fun toPredicate(
        root: Root<Dashboard>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<String>("key"))
        query.groupBy(groupList)

        val predicates = permissions.stream()
            .filter { permission: Permission ->
                Dashboard::class.java == permission.resourceType
                    && authRequest.action == permission.action
            }
            .map { permission: Permission ->
                permission.toPredicate(
                    root,
                    query,
                    criteriaBuilder,
                    authRequest.resourceType,
                    queryDialectHelper
                )
            }.toList()
        return combinePredicates(criteriaBuilder, predicates)
    }
}