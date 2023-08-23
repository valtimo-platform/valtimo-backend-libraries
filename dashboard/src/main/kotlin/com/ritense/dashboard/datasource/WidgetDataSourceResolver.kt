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

import com.ritense.valtimo.contract.dashboard.feature.WidgetDataFeature
import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import java.lang.reflect.Method
import mu.KLogger
import mu.KotlinLogging

class WidgetDataSourceResolver : AnnotatedClassResolver() {

    val dataSourceMethodMap: Map<WidgetDataSource, Method> = findMethodsWithAnnotation<WidgetDataSource>()
        .associateBy { it.getAnnotation(WidgetDataSource::class.java) }

    val dataFeatureClassMap: Map<Class<*>, List<WidgetDataFeature>> = findClassesWithAnnotation<WidgetDataFeature>()
        .associateWith { it.getAnnotationsByType(WidgetDataFeature::class.java).toList() }

    init {
        if (logger.isWarnEnabled) {
            val duplicates = dataFeatureClassMap.flatMap { it.value }
                .groupBy { it.value }
                .filterValues { it.size > 1 }
                .keys

            require(duplicates.isEmpty()) {
                logger.warn { "Found duplicate widget dataFeature values: $duplicates. Consider implementing interfaces." }
            }
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}