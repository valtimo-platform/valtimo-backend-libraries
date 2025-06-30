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
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.processdocument.service.TASK_SEARCH_FIELD_OWNER_TYPE
import com.ritense.search.deployment.ReadFileSearchFieldDto
import com.ritense.search.importer.SearchFieldImporter
import com.ritense.search.service.SearchFieldV2Service

class TaskSearchFieldImporter(
    private val objectMapper: ObjectMapper,
    searchFieldService: SearchFieldV2Service,
) : SearchFieldImporter(searchFieldService, OWNER_TYPE_KEY) {

    override fun dependsOn(): Set<String> = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val searchFields = objectMapper.readValue<List<ReadFileSearchFieldDto>>(
            request.content.toString(Charsets.UTF_8)
        )
        deploy(request.caseDefinitionId!!.key, searchFields)
    }

    companion object {
        private const val OWNER_TYPE_KEY = TASK_SEARCH_FIELD_OWNER_TYPE
        private val FILENAME_REGEX = """/task-search-field/([^/]+)\.task-search-field.json""".toRegex()
    }
}