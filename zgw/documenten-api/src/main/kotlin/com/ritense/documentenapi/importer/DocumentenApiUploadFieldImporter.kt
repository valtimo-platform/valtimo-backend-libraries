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

package com.ritense.documentenapi.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.documentenapi.deployment.ZgwDocumentUploadField
import com.ritense.documentenapi.domain.DocumentenApiUploadField
import com.ritense.documentenapi.domain.DocumentenApiUploadFieldId
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.ZGW_DOCUMENT_UPLOAD_FIELD
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import io.github.oshai.kotlinlogging.KotlinLogging

class DocumentenApiUploadFieldImporter(
    private val objectMapper: ObjectMapper,
    private val documentenApiService: DocumentenApiService,
) : Importer {
    override fun type(): String = ZGW_DOCUMENT_UPLOAD_FIELD

    override fun dependsOn(): Set<String> {
        return setOf(DOCUMENT_DEFINITION)
    }

    override fun supports(fileName: String): Boolean = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        logger.info { "Importing ZGW document upload fields for file ${request.fileName}" }
        deploy(request.caseDefinitionId!!, request.content.toString(Charsets.UTF_8))
    }

    private fun deploy(caseDefinitionId: CaseDefinitionId, content: String) {
        val fields = getJson(content)
        runWithoutAuthorization {
            fields.forEach { field ->
                documentenApiService.updateUploadField(
                    DocumentenApiUploadField(
                        id = DocumentenApiUploadFieldId(caseDefinitionId.key, field.key),
                        defaultValue = field.defaultValue,
                        visible = field.visible,
                        readonly = field.readonly,
                    )
                )
            }
        }
    }

    private fun getJson(rawJson: String): List<ZgwDocumentUploadField> {
        return objectMapper.readValue<List<ZgwDocumentUploadField>>(rawJson)
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        val FILENAME_REGEX = """/zgw/document-upload-field/([^/]+)\.zgw-document-upload-field\.json"""
            .toRegex()
    }
}