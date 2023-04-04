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

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.note.domain.Note
import java.util.UUID
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class NoteDocumentMapper(): AuthorizationEntityMapper<Note, JsonSchemaDocument> {
    override fun mapTo(entity: Note): List<JsonSchemaDocument> {
        TODO("Not yet implemented")
    }

    override fun mapQueryTo(root: Root<Note>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): AuthorizationEntityMapperResult<JsonSchemaDocument> {
        val documentRoot: Root<JsonSchemaDocument> = query.from(JsonSchemaDocument::class.java)
        return AuthorizationEntityMapperResult(
            documentRoot,
            query,
            criteriaBuilder.equal(root.get<UUID>("documentId"), documentRoot.get<JsonSchemaDocumentId>("id").get<UUID>("id"))
        )
    }

    override fun appliesTo(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == Note::class.java && toClass == JsonSchemaDocument::class.java
    }
}