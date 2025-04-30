/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.deployment.CaseTagDto
import com.ritense.document.service.CaseTagService
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_TAG
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseTagImporter(
    private val objectMapper: ObjectMapper,
    private val caseTagService: CaseTagService,
) : Importer {
    override fun type() = CASE_TAG

    override fun dependsOn() = setOf(CASE_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val caseTags = objectMapper.readValue(
            request.content.toString(Charsets.UTF_8),
            object : TypeReference<List<CaseTagDto>>() {})
        deploy(caseTags, request.caseDefinitionId!!)
    }

    private fun deploy(caseTags: List<CaseTagDto>, caseDefinitionId: CaseDefinitionId) {
        AuthorizationContext.runWithoutAuthorization {
            caseTags.forEach {
                if (!caseTagService.exists(caseDefinitionId, it.key)) {
                    caseTagService.create(
                        caseDefinitionId,
                        CaseTagCreateRequestDto(
                            it.key,
                            it.title,
                            it.color
                        )
                    )
                } else {
                    caseTagService.update(
                        caseDefinitionId,
                        it.key,
                        CaseTagUpdateRequestDto(
                            it.key,
                            it.title,
                            it.color
                        )
                    )
                }
            }
        }
    }

    private companion object {
        val FILENAME_REGEX = """/case/tag/([^/]+)\.case-tag\.json""".toRegex()
    }
}