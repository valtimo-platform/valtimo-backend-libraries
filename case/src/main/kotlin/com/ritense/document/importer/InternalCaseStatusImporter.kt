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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.service.InternalCaseStatusService
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateRequestDto
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.INTERNAL_CASE_STATUS
import org.springframework.transaction.annotation.Transactional

@Transactional
class InternalCaseStatusImporter(
    private val objectMapper: ObjectMapper,
    private val internalCaseStatusService: InternalCaseStatusService,
) : Importer {
    override fun type() = INTERNAL_CASE_STATUS

    override fun dependsOn() = setOf(CASE_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val internalCaseStatuses = objectMapper.readValue<List<InternalCaseStatusDto>>(request.content)
        deploy(request.caseDefinitionId!!.key, internalCaseStatuses)
    }

    private fun deploy(caseDefinitionKey: String, internalCaseStatuses: List<InternalCaseStatusDto>) {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatuses.forEach {
                if (!internalCaseStatusService.exists(caseDefinitionKey, it.key)) {
                    internalCaseStatusService.create(
                        caseDefinitionKey,
                        InternalCaseStatusCreateRequestDto(
                            it.key,
                            it.title,
                            it.visibleInCaseListByDefault,
                            it.color
                        )
                    )
                } else {
                    internalCaseStatusService.update(
                        caseDefinitionKey,
                        it.key,
                        InternalCaseStatusUpdateRequestDto(
                            it.key,
                            it.title,
                            it.visibleInCaseListByDefault,
                            it.color
                        )
                    )
                }
            }
        }
    }

    private companion object {
        val FILENAME_REGEX = """/case/internal-status/([^/]+)\.internal-case-status\.json""".toRegex()
    }
}