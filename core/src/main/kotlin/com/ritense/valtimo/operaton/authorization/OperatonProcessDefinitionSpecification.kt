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

package com.ritense.valtimo.operaton.authorization

import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionRepository
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

class OperatonProcessDefinitionSpecification(
    authRequest: AuthorizationRequest<OperatonProcessDefinition>,
    permissions: List<Permission>,
    private val repository: OperatonProcessDefinitionRepository,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<OperatonProcessDefinition>(authRequest, permissions) {
    override fun identifierToEntity(identifier: String): OperatonProcessDefinition {
        return repository.findById(identifier).get()
    }

    override fun toPredicate(root: Root<OperatonProcessDefinition>, query: AbstractQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate {
        val predicates = permissions
            .filter { permission ->
                OperatonProcessDefinition::class.java == permission.resourceType
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
            }
        return combinePredicates(criteriaBuilder, predicates)
    }
}

