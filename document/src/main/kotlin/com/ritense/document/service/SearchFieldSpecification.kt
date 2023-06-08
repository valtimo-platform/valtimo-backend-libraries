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
package com.ritense.document.service

import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.permission.Permission
import com.ritense.document.domain.impl.searchfield.SearchField
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class SearchFieldSpecification(
    authContext: AuthorizationRequest<SearchField>,
    permissions: List<Permission>,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<SearchField>(authContext, permissions) {

    override fun toPredicate(
        root: Root<SearchField>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        if (query.groupList.isEmpty()) {
            val groupList = ArrayList(query.groupList)
            groupList.add(root.get<Any>("id").get<Any>("id"))
            query.groupBy(groupList)
        }
        val predicates = permissions
            .filter { permission: Permission ->
                SearchField::class.java == permission.resourceType && authContext.action == permission.action
            }
            .map { permission: Permission ->
                permission.toPredicate(
                    root,
                    query,
                    criteriaBuilder,
                    authContext.resourceType,
                    queryDialectHelper
                )
            }
        return combinePredicates(criteriaBuilder, predicates)
    }
}