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

package com.ritense.document

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.repository.DocumentDefinitionRepository
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root

class DocumentDocumentDefinitionMapper(
    private val documentDefinitionRepository: DocumentDefinitionRepository<JsonSchemaDocumentDefinition>
) : AuthorizationEntityMapper<JsonSchemaDocument, JsonSchemaDocumentDefinition> {
    override fun mapRelated(entity: JsonSchemaDocument): List<JsonSchemaDocumentDefinition> {
        return runWithoutAuthorization {
            listOf(documentDefinitionRepository.findById(entity.definitionId()).get())
        }
    }

    override fun mapQuery(
        root: Root<JsonSchemaDocument>,
        query: AbstractQuery<*>, criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<JsonSchemaDocumentDefinition> {
        val definitionRoot: Root<JsonSchemaDocumentDefinition> = query.from(JsonSchemaDocumentDefinition::class.java)
        query.groupBy(query.groupList + root.get<JsonSchemaDocumentDefinitionId>("documentDefinitionId"))

        return AuthorizationEntityMapperResult(
            definitionRoot,
            query,
            criteriaBuilder.equal(
                root.get<JsonSchemaDocumentDefinitionId>("documentDefinitionId"),
                definitionRoot.get<JsonSchemaDocumentDefinitionId>("id")
            )
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == JsonSchemaDocument::class.java && toClass == JsonSchemaDocumentDefinition::class.java
    }
}