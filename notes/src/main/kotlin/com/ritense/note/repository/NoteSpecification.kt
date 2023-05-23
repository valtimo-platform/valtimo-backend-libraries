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

package com.ritense.note.repository

import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.AuthorizationRequest
import com.ritense.authorization.permission.Permission
import com.ritense.note.domain.Note
import com.ritense.valtimo.contract.database.QueryDialectHelper
import java.util.UUID
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class NoteSpecification(
    permissions: List<Permission>,
    authContext: AuthorizationRequest<Note>,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecification<Note>(permissions, authContext) {
    override fun toPredicate(
        root: Root<Note>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        // Filter the permissions for the relevant ones and use those to  find the filters that are required
        // Turn those filters into predicates
        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<UUID>("id"))
        query.groupBy(groupList)

        val predicates = permissions.stream()
            .filter { permission: Permission ->
                Note::class.java == permission.resourceType &&
                    authContext.action == permission.action
            }
            .map { permission: Permission ->
                permission.toPredicate(
                    root,
                    query,
                    criteriaBuilder,
                    authContext.resourceType,
                    queryDialectHelper
                )
            }.toList()
        return combinePredicates(criteriaBuilder, predicates)
    }
}

