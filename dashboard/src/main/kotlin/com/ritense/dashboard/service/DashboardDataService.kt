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

package com.ritense.dashboard.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.dashboard.datasource.WidgetDataSourceResolver
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.web.rest.dto.DashboardWidgetDataResultDto
import org.springframework.context.ApplicationContext
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class DashboardDataService(
    private val applicationContext: ApplicationContext,
    private val widgetDataSourceResolver: WidgetDataSourceResolver,
    private val widgetConfigurationRepository: WidgetConfigurationRepository,
    private val objectMapper: ObjectMapper
) {

    fun getWidgetDataForDashboard(dashboardKey: String): List<DashboardWidgetDataResultDto> {
        val dataSourceMap = widgetDataSourceResolver.widgetDataSourceMap
            .mapKeys { it.key.key }

        return widgetConfigurationRepository.findAllByDashboardKey(dashboardKey)
            .sortedBy { it.order }
            .associateWith { dataSourceMap[it.dataSourceKey]?: throw RuntimeException("Could not find data source for ${it.dataSourceKey}") }
            .map { (config, method) ->
                val arguments = when (method.parameterCount) {
                    0 -> {
                        emptyArray<Any>()
                    }
                    else -> {
                        arrayOf(objectMapper.treeToValue(config.dataSourceProperties, method.parameterTypes.single()))
                    }
                }
                val datasourceBean = applicationContext.getBean(method.declaringClass)
                val dataResult = method.invoke(datasourceBean, *arguments)

                DashboardWidgetDataResultDto(config.key, dataResult)
            }
    }

}