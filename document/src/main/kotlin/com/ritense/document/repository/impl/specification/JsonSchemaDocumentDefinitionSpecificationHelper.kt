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

package com.ritense.document.repository.impl.specification

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import java.util.UUID
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class JsonSchemaDocumentDefinitionSpecificationHelper {

    companion object {
        @JvmStatic
        fun byIdName(name: String): Specification<JsonSchemaDocumentDefinition> {
            return Specification { root: Root<JsonSchemaDocumentDefinition>,
                                   _: CriteriaQuery<*>,
                                   criteriaBuilder: CriteriaBuilder ->
                criteriaBuilder.equal(root.get<Any>(ID).get<String>(NAME), name)
            }
        }

        @JvmStatic
        fun byLatestVersion(): Specification<JsonSchemaDocumentDefinition> {
            return Specification { root: Root<JsonSchemaDocumentDefinition>,
                                   query: CriteriaQuery<*>,
                                   cb: CriteriaBuilder ->

                val sub = query.subquery(Long::class.java)
                val subRoot = sub.from(JsonSchemaDocumentDefinition::class.java)
                sub.select(cb.max(subRoot.get<Any>(ID).get(VERSION)))
                sub.where(
                    cb.and(
                        cb.equal(subRoot.get<Any>(ID).get<String>(NAME), root.get<Any>(ID).get<String>(NAME)),
                    )
                )

                cb.equal(root.get<Any>(ID).get<Long>(VERSION), sub)
            }
        }

        private const val ID: String = "id"
        private const val VERSION: String = "version"
        private const val NAME: String = "name"
    }
}