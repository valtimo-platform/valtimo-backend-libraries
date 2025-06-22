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
package com.ritense.document.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshotId
import com.ritense.document.repository.DocumentSnapshotRepository
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.UUID

class JsonSchemaDocumentSnapshotSpecification(
    authRequest: AuthorizationRequest<JsonSchemaDocumentSnapshot>,
    permissions: List<Permission>,
    private val queryDialectHelper: QueryDialectHelper,
    private val repository: DocumentSnapshotRepository<JsonSchemaDocumentSnapshot>
) : AuthorizationSpecification<JsonSchemaDocumentSnapshot>(authRequest, permissions) {

    override fun toPredicate(
        root: Root<JsonSchemaDocumentSnapshot>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        // Filter the permissions for the relevant ones and use those to  find the filters that are required
        // Turn those filters into predicates
        if (query.groupList.isEmpty()) {
            val groupList = ArrayList(query.groupList)
            groupList.add(root.get<Any>("id").get<Any>("id"))
            query.groupBy(groupList)
        }
        val predicates = permissions
            .filter { permission: Permission ->
                JsonSchemaDocumentSnapshot::class.java == permission.resourceType && authRequest.action == permission.action
            }
            .map { permission: Permission ->
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

    override fun identifierToEntity(identifier: String): JsonSchemaDocumentSnapshot {
        return runWithoutAuthorization {
            repository.findById(JsonSchemaDocumentSnapshotId.existingId(UUID.fromString(identifier))).get()
        }
    }
}