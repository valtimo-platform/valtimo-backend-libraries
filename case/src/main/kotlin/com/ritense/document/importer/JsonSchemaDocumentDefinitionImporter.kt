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

package com.ritense.document.importer

import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import org.springframework.transaction.annotation.Transactional

@Transactional
class JsonSchemaDocumentDefinitionImporter(
    private val jsonSchemaDocumentDefinitionService: JsonSchemaDocumentDefinitionService
) : Importer {
    override fun type() = DOCUMENT_DEFINITION

    override fun dependsOn() = setOf(CASE_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(PATH_REGEX)

    override fun import(request: ImportRequest) {
        jsonSchemaDocumentDefinitionService.deploy(JsonSchema.fromString(request.content.toString(Charsets.UTF_8)), request.caseDefinitionId)
    }

    private companion object {
        val PATH_REGEX = """/document/definition/[^/]+\.json""".toRegex()
    }
}