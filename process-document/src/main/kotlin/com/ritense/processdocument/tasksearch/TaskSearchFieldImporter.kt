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

package com.ritense.processdocument.tasksearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.SEARCH_FIELD
import com.ritense.processdocument.service.SEARCH_FIELD_OWNER_TYPE
import com.ritense.search.deployment.ReadFileSearchFieldDto
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.valtimo.contract.case_.CaseDefinitionId

class TaskSearchFieldImporter(
    private val objectMapper: ObjectMapper,
    private val repository: SearchFieldV2Repository,
    private val searchFieldService: SearchFieldV2Service,
) : Importer {
    override fun type(): String = SEARCH_FIELD
    override fun dependsOn(): Set<String> = setOf(DOCUMENT_DEFINITION)
    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        deploy(request.caseDefinitionId!!, request.content.toString(Charsets.UTF_8))
    }

    fun deploy(caseDefinitionId: CaseDefinitionId, content: String) {
        repository.deleteAllByOwnerTypeAndOwnerId(OWNER_TYPE_KEY, caseDefinitionId.key)

        val searchFields = getJson(content)

        val ownerId = caseDefinitionId.key
        searchFields.mapIndexed { index, searchField ->
            val mappedField = searchField.toSearchFieldDto(ownerId, OWNER_TYPE_KEY, index)
            repository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(OWNER_TYPE_KEY, ownerId, mappedField.key)
                ?.let { _ ->
                    searchFieldService.update(ownerId, searchField.key, mappedField)
                } ?: searchFieldService.create(mappedField)
        }
    }

    private fun getJson(rawJson: String): List<ReadFileSearchFieldDto> {
        return objectMapper.readValue<List<ReadFileSearchFieldDto>>(rawJson)
    }

    companion object {
        private const val OWNER_TYPE_KEY = SEARCH_FIELD_OWNER_TYPE
        private val FILENAME_REGEX = """/task-search-field/([^/]+)\.task-search-field.json""".toRegex()
    }
}