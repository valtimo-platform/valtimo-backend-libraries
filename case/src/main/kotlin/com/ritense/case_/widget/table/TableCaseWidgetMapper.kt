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

package com.ritense.case_.widget.table

import com.ritense.case_.domain.tab.CaseWidgetTabWidgetId
import com.ritense.case_.widget.CaseWidgetMapper

class TableCaseWidgetMapper : CaseWidgetMapper<TableCaseWidget, TableCaseWidgetDto> {

    override fun toDto(entity: TableCaseWidget) = TableCaseWidgetDto(
        key = entity.id.key,
        title = entity.title,
        width = entity.width,
        highContrast = entity.highContrast,
        actions = entity.actions,
        properties = entity.properties
    )

    override fun toEntity(dto: TableCaseWidgetDto, index: Int) = TableCaseWidget(
        id = CaseWidgetTabWidgetId(dto.key),
        title = dto.title,
        width = dto.width,
        highContrast = dto.highContrast,
        actions = dto.actions ?: emptyList(),
        properties = dto.properties,
        order = index
    )
}