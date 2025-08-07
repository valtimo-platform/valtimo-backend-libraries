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

package com.ritense.case.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.deployment.CaseTabDto
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.case.domain.CaseTabType
import com.ritense.case.repository.CaseTabRepository
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_TAB
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseTabImporter(
    private val objectMapper: ObjectMapper,
    private val caseTabRepository: CaseTabRepository,
) : Importer {
    override fun type() = CASE_TAB

    override fun dependsOn() = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        deploy(request.content.toString(Charsets.UTF_8), request.caseDefinitionId!!)
    }

    private fun deploy(fileContent: String, caseDefinitionId: CaseDefinitionId) {
        val tabs = try {
            objectMapper.readValue(fileContent, object : TypeReference<List<CaseTabDto>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse file content as valid case widget tabs: ${e.message}", e)
        }

        val toSave = tabs.mapIndexed { index, tab ->
            CaseTab(
                id = CaseTabId(caseDefinitionId, tab.key),
                name = tab.name,
                tabOrder = index,
                type = tab.type,
                contentKey = tab.contentKey,
                showTasks = tab.showTasks
            )
        }

        caseTabRepository.saveAll(toSave)
    }

    private companion object {
        private val FILENAME_REGEX = """/case/tab/([^/]+)\.case-tab\.json""".toRegex()
    }
}