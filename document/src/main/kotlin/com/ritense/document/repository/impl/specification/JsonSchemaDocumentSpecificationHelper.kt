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
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class JsonSchemaDocumentSpecificationHelper {

    companion object {
        @JvmStatic
        fun byDocumentDefinitionIdName(name: String): Specification<JsonSchemaDocument> {
            return Specification { root: Root<JsonSchemaDocument>,
                                   _: CriteriaQuery<*>?,
                                   criteriaBuilder: CriteriaBuilder ->
                criteriaBuilder.equal(root.get<Any>("documentDefinitionId").get<String>("name"), name)
            }
        }

        @JvmStatic
        fun byDocumentDefinitionIdVersion(version: Long): Specification<JsonSchemaDocument> {
            return Specification { root: Root<JsonSchemaDocument>,
                                   _: CriteriaQuery<*>?,
                                   criteriaBuilder: CriteriaBuilder ->
                criteriaBuilder.equal(root.get<Any>("documentDefinitionId").get<String>("version"), version)
            }
        }

        @JvmStatic
        fun byDocumentDefinitionId(id: DocumentDefinition.Id): Specification<JsonSchemaDocument> {
            return byDocumentDefinitionIdName(id.name()).and(byDocumentDefinitionIdVersion(id.version()))
        }
    }
}