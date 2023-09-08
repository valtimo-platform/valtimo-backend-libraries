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

package com.ritense.document.repository.impl

import com.ritense.document.domain.impl.JsonSchemaDocument
import org.springframework.data.jpa.domain.Specification

class JsonSchemaDocumentSpecificationHelper {

    companion object {

        const val DOCUMENT_DEFINITION_ID: String = "documentDefinitionId"
        const val NAME: String = "name"
        const val ASSIGNEE_ID: String = "assigneeId"

        @JvmStatic
        fun byDocumentDefinitionName(documentDefinitionName: String) =
            Specification<JsonSchemaDocument> { root, _, cb ->
                cb.equal(root.get<Any>(DOCUMENT_DEFINITION_ID).get<Any>(NAME), documentDefinitionName)
            }

        @JvmStatic
        fun byUnassigned() = Specification<JsonSchemaDocument> { root, _, cb ->
            cb.isNull(root.get<Any>(ASSIGNEE_ID))
        }
    }

}