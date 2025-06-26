/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.authorization

import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.repository.IkoDataAggregateRepository
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

class IkoDataAggregateSpecification(
    authRequest: AuthorizationRequest<IkoDataAggregate>,
    permissions: List<Permission>,
    private val ikoDataAggregateRepository: IkoDataAggregateRepository,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<IkoDataAggregate>(authRequest, permissions) {

    override fun toPredicate(
        root: Root<IkoDataAggregate>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        val predicates = permissions.stream()
            .filter { permission ->
                IkoDataAggregate::class.java == permission.resourceType
                    && authRequest.action == permission.action
            }
            .map { permission ->
                permission.toPredicate(
                    root,
                    query,
                    criteriaBuilder,
                    authRequest,
                    queryDialectHelper
                )
            }.toList()
        return combinePredicates(criteriaBuilder, predicates)
    }

    override fun identifierToEntity(key: String): IkoDataAggregate {
        return ikoDataAggregateRepository.getReferenceById(key)
    }
}


