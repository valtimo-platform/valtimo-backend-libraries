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

package com.ritense.documentenapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.documentenapi.repository.ZgwDocumentTrefwoordRepository
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.ZGW_DOCUMENT_TREFWOORD
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@Transactional
class ZgwDocumentTrefwoordImporter(
    private val zgwDocumentTrefwoordRepository: ZgwDocumentTrefwoordRepository,
    private val zgwDocumentTrefwoordService: ZgwDocumentTrefwoordService,
    private val objectMapper: ObjectMapper
) : Importer {
    override fun type() = ZGW_DOCUMENT_TREFWOORD

    override fun dependsOn() = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        logger.info { "Importing ZGW document trefwoorden for file ${request.fileName}" }
        deploy(request.caseDefinitionId!!, request.content.toString(Charsets.UTF_8))
    }

    private fun deploy(caseDefinitionId: CaseDefinitionId, content: String) {
        val trefwoorden = getJson(content)
        runWithoutAuthorization {
            zgwDocumentTrefwoordRepository.deleteAll(
                zgwDocumentTrefwoordRepository.findAllByCaseDefinitionName(
                    caseDefinitionId.key,
                    Pageable.unpaged()
                )
            )
            trefwoorden.forEach { trefwoord ->
                zgwDocumentTrefwoordService.createTrefwoord(caseDefinitionId.key, trefwoord)
            }
        }
    }

    private fun getJson(rawJson: String): List<String> {
        return objectMapper.readValue<List<String>>(rawJson)
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        val FILENAME_REGEX = """/zgw/trefwoord/([^/]+)\.zgw-document-trefwoord\.json""".toRegex()
    }
}