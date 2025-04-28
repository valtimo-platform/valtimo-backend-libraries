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

package com.ritense.document.other

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.impl.JsonSchemaDocumentService
import mu.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LostUpdateService(
    private val documentService: JsonSchemaDocumentService,
    private val jdbcTemplate: JdbcTemplate,
) {

    @Transactional
    @RunWithoutAuthorization
    fun writeDocumentContent(documentId: JsonSchemaDocumentId, index: Int) {
        val document = documentService.get(documentId.id.toString())
        val content = document.content().asJson() as ObjectNode
        logger.info { "$index -> $content" }
        content.put("index_$index", index)
        documentService.modifyDocument(document, content)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
