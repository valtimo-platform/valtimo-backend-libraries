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

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
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
                sub.select(cb.max(subRoot.get<Any>(ID).get<CaseDefinitionId>(CASE_DEFINITION_ID).get(VERSION_TAG)))
                sub.where(
                    cb.and(
                        cb.equal(subRoot.get<Any>(ID).get<String>(NAME), root.get<Any>(ID).get<String>(NAME)),
                    )
                )

                cb.equal(root.get<Any>(ID).get<CaseDefinitionId>(CASE_DEFINITION_ID).get<Long>(VERSION_TAG), sub)
            }
        }

        @JvmStatic
        fun byIdCaseDefinitionId(caseDefinitionId: CaseDefinitionId): Specification<JsonSchemaDocumentDefinition> {
            return Specification { root: Root<JsonSchemaDocumentDefinition>,
                                   _: CriteriaQuery<*>,
                                   criteriaBuilder: CriteriaBuilder ->
                val caseDefinitionIdPath = root.get<Any>(ID).get<String>(CASE_DEFINITION_ID)
                criteriaBuilder.equal(caseDefinitionIdPath.get<String>(KEY), caseDefinitionId.key)
                criteriaBuilder.equal(caseDefinitionIdPath.get<String>(VERSION_TAG), caseDefinitionId.key)
            }
        }

        private const val ID: String = "id"
        private const val CASE_DEFINITION_ID: String = "caseDefinitionId"
        private const val KEY: String = "name"
        private const val VERSION_TAG: String = "versionTag"
        private const val NAME: String = "name"
    }
}