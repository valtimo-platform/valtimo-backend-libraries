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

package com.ritense.valtimo.camunda.authorization

import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.valtimo.camunda.domain.CamundaIdentityLink
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class CamundaIdentityLinkSpecification(
        authRequest: AuthorizationRequest<CamundaIdentityLink>,
        permissions: List<Permission>,
        private val camundaRuntimeService: CamundaRuntimeService,
        private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<CamundaIdentityLink>(authRequest, permissions) {
    override fun toPredicate(
        root: Root<CamundaIdentityLink>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        val predicates = permissions
            .filter { permission ->
                CamundaIdentityLink::class.java == permission.resourceType &&
                    authRequest.action == permission.action
            }
            .map { permission ->
                permission.toPredicate(
                    root,
                    query,
                    criteriaBuilder,
                    authRequest.resourceType,
                    queryDialectHelper
                )
            }
        return combinePredicates(criteriaBuilder, predicates)
    }

    override fun identifierToEntity(identifier: String): CamundaIdentityLink {
        return camundaRuntimeService.getIdentityLink(identifier)
            ?: throw IllegalStateException("Identity link not found")
    }
}

