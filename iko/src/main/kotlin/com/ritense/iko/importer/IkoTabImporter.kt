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
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_TAB
import com.ritense.tab.importer.TabImporter
import com.ritense.tab.service.TabService

class IkoTabImporter(
    private val objectMapper: ObjectMapper,
    tabService: TabService,
) : TabImporter(tabService, IKO_TAB_OWNER) {

    override fun type(): String = IKO_TAB

    override fun dependsOn(): Set<String> = setOf(IKO_DATA_REQUEST)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val ikoTabs = objectMapper.readValue<IkoTabsDto>(request.content.toString(Charsets.UTF_8))
        deploy(ikoTabs.ikoDataAggregateKey, ikoTabs.ikoTabs)
    }

    override fun partOfCaseDefinition(): Boolean = false

    companion object {
        const val IKO_TAB_OWNER = "IkoDataAggregate"
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-tab\.json""".toRegex()
    }
}