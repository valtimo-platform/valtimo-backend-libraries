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

package com.ritense.case_.widget.fields

import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.widget.CaseWidgetDataProvider
import com.ritense.valueresolver.ValueResolverService
import org.springframework.data.domain.Pageable
import java.util.UUID

class FieldsCaseWidgetDataProvider(
    private val valueResolverService: ValueResolverService
) : CaseWidgetDataProvider<FieldsCaseWidget> {

    override fun supportedWidgetType() = FieldsCaseWidget::class.java

    override fun getData(documentId: UUID, widgetTab: CaseWidgetTab, widget: FieldsCaseWidget, pageable: Pageable): Any {
        val valueKeyMap = widget.properties.columns.flatMap { column ->
            column.map { field ->
                field.value to field.key
            }
        }.toMap()

        val resolvedValues = valueResolverService.resolveValues(documentId.toString(), valueKeyMap.keys)

        return resolvedValues.map { (placeholder, value) ->
            valueKeyMap[placeholder] to value
        }.toMap()
    }

}