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

package com.ritense.document.repository.impl.specification

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class JsonSchemaDocumentSnapshotSpecificationHelper {

    companion object {

        @JvmStatic
        fun bySearch(
            documentDefinitionName: String?,
            documentId: JsonSchemaDocumentId?,
            createdOnFrom: LocalDateTime?,
            createdOnTo: LocalDateTime?
        ): Specification<JsonSchemaDocumentSnapshot> {
            return Specification { root: Root<JsonSchemaDocumentSnapshot>,
                                   _: CriteriaQuery<*>?,
                                   criteriaBuilder: CriteriaBuilder ->
                val predicates = mutableListOf<Predicate>()

                if(documentDefinitionName != null) {
                    predicates.add(
                        criteriaBuilder.equal(
                            root.get<Any>("document").get<Any>("documentDefinitionId").get<String>("name"),
                            documentDefinitionName
                        )
                    )
                }

                if(documentId != null) {
                    predicates.add(
                        criteriaBuilder.equal(root.get<Any>("document").get<JsonSchemaDocumentId>("id"), documentId)
                    )
                }

                if(createdOnFrom != null) {
                    predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), createdOnFrom)
                    )
                }

                if(createdOnTo != null) {
                    predicates.add(
                        criteriaBuilder.lessThan(root.get("createdOn"), createdOnTo)
                    )
                }

                if(predicates.isNotEmpty()) {
                    criteriaBuilder.and(*predicates.toTypedArray())
                } else {
                    null
                }
            }
        }
    }
}