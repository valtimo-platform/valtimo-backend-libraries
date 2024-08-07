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

package com.ritense.note.repository

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.note.domain.Note
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root
import java.util.UUID

class NoteDocumentMapper(
    private val documentRepository: JsonSchemaDocumentRepository
) : AuthorizationEntityMapper<Note, JsonSchemaDocument> {
    override fun mapRelated(entity: Note): List<JsonSchemaDocument> {
        return runWithoutAuthorization { listOf(documentRepository.findById(JsonSchemaDocumentId.existingId(entity.documentId)).get()) }
    }

    override fun mapQuery(root: Root<Note>, query: AbstractQuery<*>, criteriaBuilder: CriteriaBuilder): AuthorizationEntityMapperResult<JsonSchemaDocument> {
        val documentRoot: Root<JsonSchemaDocument> = query.from(JsonSchemaDocument::class.java)
        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<UUID>("documentId"))
        query.groupBy(groupList)

        return AuthorizationEntityMapperResult(
            documentRoot,
            query,
            criteriaBuilder.equal(root.get<UUID>("documentId"), documentRoot.get<JsonSchemaDocumentId>("id").get<UUID>("id"))
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == Note::class.java && toClass == JsonSchemaDocument::class.java
    }
}