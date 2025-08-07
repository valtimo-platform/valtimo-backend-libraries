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

package com.ritense.case_.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.domain.CaseTabId
import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import com.ritense.case_.widget.CaseWidgetMapper
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_TAB
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_WIDGET_TAB
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.FORM
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.validation.check
import jakarta.validation.Validator

class CaseWidgetTabImporter(
    private val objectMapper: ObjectMapper,
    private val validator: Validator,
    private val caseWidgetTabRepository: CaseWidgetTabRepository,
    private val caseWidgetMappers: List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
) : Importer {
    override fun type() = CASE_WIDGET_TAB

    override fun dependsOn() = setOf(DOCUMENT_DEFINITION, CASE_TAB, FORM)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        return deploy(request.content.toString(Charsets.UTF_8), request.caseDefinitionId!!)
    }

    fun deploy(fileContent: String, caseDefinitionId: CaseDefinitionId) {
        val tabs = try {
            objectMapper.readValue(fileContent, object : TypeReference<List<CaseWidgetTabDto>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse file content as valid case widget tabs: ${e.message}", e)
        }

        validator.check(tabs)
        tabs.forEach { it.validate(caseDefinitionId) }

        val toSave = tabs.map { tab ->
            CaseWidgetTab(
                CaseTabId(
                    caseDefinitionId = caseDefinitionId,
                    key = tab.key
                ),
                widgets = tab.widgets.mapIndexed { index, widgetDto ->
                    caseWidgetMappers.first { mapper ->
                        mapper.supportedDtoType().isAssignableFrom(widgetDto::class.java)
                    }.toEntity(widgetDto, index)
                }
            )
        }

        caseWidgetTabRepository.saveAll(toSave)
    }

    private companion object {
        val FILENAME_REGEX = """/case/widget-tab/([^/]+)\.case-widget-tab\.json""".toRegex()
    }
}