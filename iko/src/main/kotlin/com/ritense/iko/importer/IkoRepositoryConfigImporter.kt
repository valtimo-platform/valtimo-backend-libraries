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
import com.ritense.iko.service.IkoRepositoryService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_REPOSITORY_CONFIG
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoRepositoryConfigImporter(
    private val objectMapper: ObjectMapper,
    private val ikoRepositoryService: IkoRepositoryService,
) : Importer {
    override fun type() = IKO_REPOSITORY_CONFIG

    override fun dependsOn(): Set<String> = emptySet()

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val fileContent = request.content.toString(Charsets.UTF_8)
        val ikoRepositoryConfig = objectMapper.readValue<IkoRepositoryConfigDto>(fileContent)
        ikoRepositoryService.saveIkoRepositoryConfig(
            key = ikoRepositoryConfig.key,
            title = ikoRepositoryConfig.title,
            type = ikoRepositoryConfig.type,
            properties = ikoRepositoryConfig.properties ?: emptyMap(),
        )
    }

    override fun partOfCaseDefinition(): Boolean = false

    private companion object {
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-repository-config\.json""".toRegex()
    }
}