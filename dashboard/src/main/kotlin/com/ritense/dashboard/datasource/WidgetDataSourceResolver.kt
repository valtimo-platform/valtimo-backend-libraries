/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.dashboard.datasource

import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import java.lang.reflect.Method

class WidgetDataSourceResolver : AnnotatedClassResolver() {

    @EventListener(ApplicationStartedEvent::class)
    fun loadWidgetDataSources() {
        findWidgetDataSourceClasses()
    }

    private fun findWidgetDataSourceClasses() {
        WIDGET_DATA_SOURCE_MAP = findMethodsWithAnnotation<WidgetDataSource>()
            .associateBy { it.getAnnotation(WidgetDataSource::class.java).key }
            .toSortedMap()
    }

    companion object {
        lateinit var WIDGET_DATA_SOURCE_MAP: Map<String, Method>
    }
}