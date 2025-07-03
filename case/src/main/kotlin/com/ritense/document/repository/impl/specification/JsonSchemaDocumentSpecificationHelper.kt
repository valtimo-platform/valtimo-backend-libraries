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

import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class JsonSchemaDocumentSpecificationHelper {

    companion object {

        const val DOCUMENT_DEFINITION_ID: String = "documentDefinitionId"
        const val CASE_DEFINITION_ID: String = "caseDefinitionId"
        const val NAME: String = "name"
        const val ASSIGNEE_ID: String = "assigneeId"

        @JvmStatic
        fun byDocumentDefinitionIdName(name: String): Specification<JsonSchemaDocument> {
            return Specification { root: Root<JsonSchemaDocument>,
                                   _: CriteriaQuery<*>?,
                                   criteriaBuilder: CriteriaBuilder ->
                criteriaBuilder.equal(root.get<Any>(DOCUMENT_DEFINITION_ID).get<String>(NAME), name)
            }
        }

        @JvmStatic
        fun byDocumentDefinitionIdCaseDefinitionId(caseDefinitionId: CaseDefinitionId): Specification<JsonSchemaDocument> {
            return Specification { root: Root<JsonSchemaDocument>,
                                   _: CriteriaQuery<*>?,
                                   criteriaBuilder: CriteriaBuilder ->
                criteriaBuilder.equal(
                    root.get<Any>(DOCUMENT_DEFINITION_ID).get<CaseDefinitionId>(CASE_DEFINITION_ID),
                    caseDefinitionId
                )
            }
        }

        @JvmStatic
        fun byDocumentDefinitionId(id: DocumentDefinition.Id): Specification<JsonSchemaDocument> {
            return byDocumentDefinitionIdName(id.name()).and(byDocumentDefinitionIdCaseDefinitionId(id.caseDefinitionId()))
        }

        @JvmStatic
        fun byUnassigned() = Specification<JsonSchemaDocument> { root, _, cb ->
            cb.isNull(root.get<Any>(ASSIGNEE_ID))
        }
    }
}