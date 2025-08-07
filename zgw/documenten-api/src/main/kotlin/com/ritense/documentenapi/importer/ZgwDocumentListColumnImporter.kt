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
import com.ritense.documentenapi.deployment.ZgwDocumentListColumn
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.ZGW_DOCUMENT_LIST_COLUMN
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import io.github.oshai.kotlinlogging.KotlinLogging

class ZgwDocumentListColumnImporter(
    private val objectMapper: ObjectMapper,
    private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    private val documentenApiService: DocumentenApiService,
) : Importer {
    override fun type(): String = ZGW_DOCUMENT_LIST_COLUMN

    override fun dependsOn(): Set<String> {
        return setOf(DOCUMENT_DEFINITION)
    }

    override fun supports(fileName: String): Boolean = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        logger.info { "Importing ZGW document list columns for file ${request.fileName}" }
        deploy(request.caseDefinitionId!!, request.content.toString(Charsets.UTF_8))
    }

    private fun deploy(caseDefinitionId: CaseDefinitionId, content: String) {
        runWithoutAuthorization {
            val columns = getJson(content)
            documentenApiColumnRepository.deleteAll(
                documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(
                    caseDefinitionId.key
                )
            )
            columns.forEach { column ->
                documentenApiService.createOrUpdateColumn(
                    DocumentenApiColumn(
                        id = DocumentenApiColumnId(caseDefinitionId.key, column.key),
                        defaultSort = column.defaultSort
                    )
                )
            }
        }
    }

    private fun getJson(rawJson: String): List<ZgwDocumentListColumn> {
        return objectMapper.readValue<List<ZgwDocumentListColumn>>(rawJson)
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        val FILENAME_REGEX = """/zgw/document-list-column/([^/]+)\.zgw-document-list-column\.json"""
            .toRegex()
    }
}