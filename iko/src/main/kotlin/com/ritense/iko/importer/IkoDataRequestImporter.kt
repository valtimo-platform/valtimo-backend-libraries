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
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.web.rest.request.IkoDataRequestUpdateRequest
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_DATA_AGGREGATE
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_DATA_REQUEST
import org.springframework.transaction.annotation.Transactional

@Transactional
class IkoDataRequestImporter(
    private val objectMapper: ObjectMapper,
    private val ikoDataRequestService: IkoDataRequestService,
) : Importer {
    override fun type() = IKO_DATA_REQUEST

    override fun dependsOn(): Set<String> = setOf(IKO_DATA_AGGREGATE)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val fileContent = request.content.toString(Charsets.UTF_8)
        val dataRequests = objectMapper.readValue<IkoDataRequestsDto>(fileContent)
        val dataRequestUpdates = dataRequests.ikoDataRequests.map {
            IkoDataRequestUpdateRequest(
                key = it.key,
                ikoDataAggregateKey = dataRequests.ikoDataAggregateKey,
                title = it.title,
                properties = it.properties,
            )
        }

        ikoDataRequestService.delete(ikoDataAggregateKey = dataRequests.ikoDataAggregateKey)
        ikoDataRequestService.saveIkoDataRequest(dataRequestUpdates)
    }

    override fun partOfCaseDefinition(): Boolean = false

    private companion object {
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-data-request\.json""".toRegex()
    }
}