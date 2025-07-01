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
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_LIST_COLUMN
import com.ritense.search.importer.ListColumnImporter
import com.ritense.search.service.SearchListColumnService

class IkoListColumnImporter(
    private val objectMapper: ObjectMapper,
    listColumnService: SearchListColumnService,
) : ListColumnImporter(listColumnService, IKO_LIST_OWNER) {

    override fun type(): String = IKO_LIST_COLUMN

    override fun dependsOn(): Set<String> = setOf(IKO_DATA_REQUEST)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val ikoListColumns = objectMapper.readValue<IkoListColumnsDto>(request.content.toString(Charsets.UTF_8))
        val ownerId = ikoListColumns.ikoDataAggregateKey
        deploy(ownerId, ikoListColumns.ikoListColumns)
    }

    override fun partOfCaseDefinition(): Boolean = false

    companion object {
        const val IKO_LIST_OWNER = "IkoDataAggregate"
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-list-column\.json""".toRegex()
    }
}