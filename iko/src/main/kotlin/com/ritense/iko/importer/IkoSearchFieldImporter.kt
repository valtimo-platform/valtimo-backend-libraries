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

package com.ritense.iko.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_DATA_REQUEST
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_SEARCH_FIELD
import com.ritense.search.importer.SearchFieldImporter
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.service.SearchFieldV2Service

class IkoSearchFieldImporter(
    private val objectMapper: ObjectMapper,
    repository: SearchFieldV2Repository,
    searchFieldService: SearchFieldV2Service,
) : SearchFieldImporter(objectMapper, repository, searchFieldService, IKO_OWNER_TYPE_KEY) {

    override fun type(): String = IKO_SEARCH_FIELD

    override fun dependsOn(): Set<String> = setOf(IKO_DATA_REQUEST)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val ikoSearchFields = objectMapper.readValue<IkoSearchFieldsDto>(request.content.toString(Charsets.UTF_8))
        val ownerId = ikoSearchFields.ikoDataAggregateKey + ":" + ikoSearchFields.ikoDataRequestKey
        deploy(ownerId, ikoSearchFields.ikoSearchFields)
    }

    override fun partOfCaseDefinition(): Boolean = false

    companion object {
        const val IKO_OWNER_TYPE_KEY = "IkoDataRequest"
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-search-field\.json""".toRegex()
    }
}