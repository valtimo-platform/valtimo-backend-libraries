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

package com.ritense.iko.service

import com.ritense.iko.domain.IkoTabWidget
import com.ritense.iko.domain.IkoTabWidgetId
import com.ritense.iko.repository.IkoTabWidgetRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.widget.domain.Widget
import com.ritense.widget.service.WidgetService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@SkipComponentScan
@Service
class IkoWidgetService(
    private val ikoTabService: IkoTabService,
    private val ikoTabWidgetRepository: IkoTabWidgetRepository,
    private val widgetService: WidgetService,
) {

    fun findByKey(ikoDataAggregateKey: String, tabKey: String, widgetKey: String): Widget? {
        val tab = ikoTabService.findByKey(ikoDataAggregateKey, tabKey) ?: return null
        return ikoTabWidgetRepository.findByIdTabIdAndWidgetKey(tab.id, widgetKey)?.widget
    }

    fun getByKey(ikoDataAggregateKey: String, tabKey: String, widgetKey: String): Widget {
        return findByKey(ikoDataAggregateKey, tabKey, widgetKey)
            ?: error("Widget $widgetKey not found")
    }

    fun findAllByTabKey(ikoDataAggregateKey: String, tabKey: String): List<Widget> {
        val tab = ikoTabService.getByKey(ikoDataAggregateKey, tabKey)
        return ikoTabWidgetRepository.findAllByIdTabId(tab.id).map { it.widget }
    }

    fun deleteByKey(ikoDataAggregateKey: String, tabKey: String, widgetKey: String) {
        val tab = ikoTabService.getByKey(ikoDataAggregateKey, tabKey)
        ikoTabWidgetRepository.deleteByIdTabIdAndWidgetKey(tab.id, widgetKey)
    }

    fun create(ikoDataAggregateKey: String, tabKey: String, widget: Widget): Widget {
        val tab = ikoTabService.getByKey(ikoDataAggregateKey, tabKey)
        require(ikoTabWidgetRepository.findByIdTabIdAndWidgetKey(tab.id, widget.key) == null)
        val createdWidget = widgetService.create(widget)
        ikoTabWidgetRepository.save(
            IkoTabWidget(
                id = IkoTabWidgetId(tab.id, createdWidget.id),
                widget = createdWidget,
            )
        )
        return createdWidget
    }

    fun update(ikoDataAggregateKey: String, tabKey: String, widget: Widget): Widget {
        val tab = ikoTabService.getByKey(ikoDataAggregateKey, tabKey)
        requireNotNull(ikoTabWidgetRepository.findByIdTabIdAndWidgetKey(tab.id, widget.key))
        val updatedWidget = widgetService.update(widget)
        ikoTabWidgetRepository.save(
            IkoTabWidget(
                id = IkoTabWidgetId(tab.id, updatedWidget.id),
                widget = updatedWidget,
            )
        )
        return updatedWidget
    }

    fun getWidgetData(
        ikoDataAggregateKey: String,
        tabKey: String,
        widgetKey: String,
        context: Map<String, String>,
        pageable: Pageable
    ): Any? {
        val widget = getByKey(ikoDataAggregateKey, tabKey, widgetKey)
        val contextList = context.map { it.key to it.value } + listOf(
            "ikoDataAggregateKey" to ikoDataAggregateKey,
            "tabKey" to tabKey,
            "widgetKey" to widgetKey,
        )
        return widgetService.getWidgetData(widget, contextList, pageable)
    }

}