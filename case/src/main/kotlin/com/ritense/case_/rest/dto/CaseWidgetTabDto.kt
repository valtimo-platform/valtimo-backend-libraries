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

package com.ritense.case_.rest.dto

import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.widget.CaseWidgetMapper
import com.ritense.document.domain.impl.JsonSchemaDocument
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class CaseWidgetTabDto(
    @field:NotBlank val caseDefinitionName: String,
    @field:NotBlank val key: String,
    @field:Valid val widgets: List<@Valid CaseWidgetTabWidgetDto> = listOf(),
) {
    companion object {
        @JvmStatic
        fun of(
            tab: CaseWidgetTab,
            widgetMappers: List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
            permissionCheck: (CaseWidgetTabWidget) -> Boolean
        ): CaseWidgetTabDto {
            return CaseWidgetTabDto(
                tab.id.caseDefinitionName,
                tab.id.key,
                widgets = tab.widgets
                    .filter { permissionCheck(it) }
                    .map { widget ->
                        widgetMappers.first { mapper ->
                            mapper.supportedEntityType().isAssignableFrom(widget::class.java)
                        }.toDto(widget)
                }
            )
        }

        @JvmStatic
        fun ofWithContext(
            tab: CaseWidgetTab,
            widgetMappers: List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
            permissionCheck: (CaseWidgetTabWidget, JsonSchemaDocument) -> Boolean,
            document: JsonSchemaDocument
        ): CaseWidgetTabDto {
            return CaseWidgetTabDto(
                tab.id.caseDefinitionName,
                tab.id.key,
                widgets = tab.widgets
                    .filter { permissionCheck(it, document) }
                    .map { widget ->
                        widgetMappers.first { mapper ->
                            mapper.supportedEntityType().isAssignableFrom(widget::class.java)
                        }.toDto(widget)
                    }
            )
        }
    }
}
