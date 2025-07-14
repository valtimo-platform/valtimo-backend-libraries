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
import com.ritense.iko.service.IkoListColumnService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_DATA_AGGREGATE
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_LIST_COLUMN
import java.util.UUID

class IkoListColumnImporter(
    private val objectMapper: ObjectMapper,
    private val service: IkoListColumnService,
) : Importer {

    override fun type(): String = IKO_LIST_COLUMN

    override fun dependsOn(): Set<String> = setOf(IKO_DATA_AGGREGATE)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val ikoListColumnsDto = objectMapper.readValue<IkoListColumnsDto>(request.content.toString(Charsets.UTF_8))

        val existingListColumns =
            service.findAllColumnsByIkoDataAggregateKey(ikoListColumnsDto.ikoDataAggregateKey)

        ikoListColumnsDto.ikoListColumns.forEachIndexed { index, listColumnDto ->
            val existingListColumnId = existingListColumns
                .firstOrNull { existingListColumn -> existingListColumn.key == listColumnDto.key }
                ?.id?.toString()
            if (existingListColumnId != null) {
                service.update(
                    ikoListColumnsDto.ikoDataAggregateKey,
                    listColumnDto.toEntity(existingListColumnId, index)
                )
            } else {
                service.create(
                    ikoListColumnsDto.ikoDataAggregateKey,
                    listColumnDto.toEntity(UUID.randomUUID().toString(), index)
                )
            }
        }

        existingListColumns
            .filter { existingListColumn -> ikoListColumnsDto.ikoListColumns.none { listColumnDto -> listColumnDto.key == existingListColumn.key } }
            .forEach { existingListColumn ->
                service.deleteByKey(
                    ikoListColumnsDto.ikoDataAggregateKey,
                    existingListColumn.key
                )
            }
    }

    override fun partOfCaseDefinition(): Boolean = false

    companion object {
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-list-column\.json""".toRegex()
    }
}