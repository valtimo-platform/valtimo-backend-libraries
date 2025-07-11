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

package com.ritense.widget.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valueresolver.ValueResolverService
import com.ritense.widget.WidgetDataProvider
import com.ritense.widget.domain.Widget
import com.ritense.widget.repository.WidgetRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@SkipComponentScan
@Service
class WidgetService(
    private val widgetRepository: WidgetRepository,
    private val widgetDataProviders: List<WidgetDataProvider<Widget>>,
    private val valueResolverService: ValueResolverService,
) {

    fun create(widget: Widget): Widget {
        require(findById(widget.id) == null)
        return widgetRepository.save(widget)
    }

    fun update(widget: Widget): Widget {
        val existingWidget = findById(widget.id)
            ?: throw IllegalStateException("Search list widget not found")
        return widgetRepository.save(widget.copy(id = existingWidget.id))
    }

    fun delete(widgetId: UUID) {
        return widgetRepository.deleteById(widgetId)
    }

    fun findById(id: UUID): Widget? =
        widgetRepository.findById(id).getOrNull()

    fun getById(id: UUID): Widget =
        findById(id) ?: error("Widget $id not found")

    @Transactional
    fun getWidgetData(widget: Widget, context: List<Any>, pageable: Pageable): Any? {
        return runWithoutAuthorization {
            widgetDataProviders
                .first { provider -> provider.supportedWidgetType().isAssignableFrom(widget::class.java) }
                .getData(widget, context, pageable)
        }
    }
}