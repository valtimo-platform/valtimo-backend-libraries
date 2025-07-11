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
import com.ritense.iko.service.IkoWidgetService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_DATA_REQUEST
import com.ritense.importer.ValtimoImportTypes.Companion.IKO_WIDGET
import java.util.UUID

class IkoWidgetImporter(
    private val objectMapper: ObjectMapper,
    private val service: IkoWidgetService,
) : Importer {

    override fun type(): String = IKO_WIDGET

    override fun dependsOn(): Set<String> = setOf(IKO_DATA_REQUEST)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val ikoWidgetsDto = objectMapper.readValue<IkoWidgetsDto>(request.content.toString(Charsets.UTF_8))

        val existingWidgets = service.findAllByTabKey(ikoWidgetsDto.ikoDataAggregateKey, ikoWidgetsDto.ikoTabKey)

        ikoWidgetsDto.ikoWidgets.forEachIndexed { index, widgetDto ->
            val existingWidgetId = existingWidgets.firstOrNull { existingTab -> existingTab.key == widgetDto.key }?.id
            if (existingWidgetId != null) {
                service.update(
                    ikoWidgetsDto.ikoDataAggregateKey,
                    ikoWidgetsDto.ikoTabKey,
                    widgetDto.toEntity(existingWidgetId, index)
                )
            } else {
                service.create(
                    ikoWidgetsDto.ikoDataAggregateKey,
                    ikoWidgetsDto.ikoTabKey,
                    widgetDto.toEntity(UUID.randomUUID(), index)
                )
            }

            existingWidgets
                .filter { existingWidget -> ikoWidgetsDto.ikoWidgets.none { widgetDto -> widgetDto.key == existingWidget.key } }
                .forEach { existingWidget ->
                    service.deleteByKey(
                        ikoWidgetsDto.ikoDataAggregateKey,
                        ikoWidgetsDto.ikoTabKey,
                        existingWidget.key
                    )
                }
        }
    }

    override fun partOfCaseDefinition(): Boolean = false

    companion object {
        private val FILENAME_REGEX = """/global/iko/(?:.*/)?(.+)\.iko-widget\.json""".toRegex()
    }
}