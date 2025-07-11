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

package com.ritense.widget.fields

import com.ritense.valueresolver.IkoValueResolverService
import com.ritense.widget.WidgetDataProvider
import org.springframework.data.domain.Pageable

class FieldsWidgetDataProvider(
    private val valueResolverService: IkoValueResolverService,
) : WidgetDataProvider<FieldsWidget> {

    override fun supportedWidgetType() = FieldsWidget::class.java

    override fun getData(widget: FieldsWidget, context: List<Any>, pageable: Pageable): Any {
        val valueKeyMap = widget.properties.columns.flatMap { column ->
            column.map { field ->
                field.value to field.key
            }
        }.toMap()

        val resolvedValues = valueResolverService.resolveValues(context, valueKeyMap.keys, pageable)

        return widget.properties.columns.flatMap { column ->
            column.map { field ->
                field.key to resolvedValues[field.value]
            }
        }.toMap()
    }

}