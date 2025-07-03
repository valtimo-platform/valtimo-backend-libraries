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
import com.ritense.iko.service.IkoConnectorService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_CONNECTOR_CONFIG
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoConnectorConfigImporter(
    private val objectMapper: ObjectMapper,
    private val ikoConnectorService: IkoConnectorService,
) : Importer {
    override fun type() = IKO_CONNECTOR_CONFIG

    override fun dependsOn(): Set<String> = emptySet()

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val fileContent = request.content.toString(Charsets.UTF_8)
        val connectorConfig = objectMapper.readValue<IkoConnectorConfigDto>(fileContent)
        ikoConnectorService.saveIkoConnectorConfig(
            key = connectorConfig.key,
            title = connectorConfig.title,
            type = connectorConfig.type,
            properties = connectorConfig.properties ?: emptyMap(),
        )
    }

    override fun partOfCaseDefinition(): Boolean = false

    private companion object {
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-connector-config\.json""".toRegex()
    }
}