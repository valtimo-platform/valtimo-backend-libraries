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
import com.ritense.dashboard.domain.WidgetConfiguration
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.web.rest.dto.DashboardWidgetDataResultDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationContext
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class DashboardDataService(
    private val applicationContext: ApplicationContext,
    private val widgetDataSourceResolver: WidgetDataSourceResolver,
    private val widgetConfigurationRepository: WidgetConfigurationRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * This will get all widget data for the given dashboard key
     * // TODO: Test caching
     */
    fun getWidgetDataForDashboard(dashboardKey: String): List<DashboardWidgetDataResultDto> {
        return widgetConfigurationRepository.findAllByDashboardKey(dashboardKey)
            .sortedBy { it.order }
            .map { config ->
                self().getWidgetDataByConfig(config)
            }
    }

    /**
     * This can be used to get a single result by widget configuration key
     * // TODO: Test functionality
     * // TODO: Test caching
     */
    @Cacheable(value = [CACHE_NAME], key = "#key")
    fun getWidgetDataByConfigKey(key: String): DashboardWidgetDataResultDto {
        val config = widgetConfigurationRepository.getReferenceById(key)
        return self().getWidgetDataByConfig(config)
    }

    /**
     * This can be used to get a single result by widget configuration instance
     * // TODO: Test caching
     */
    @Cacheable(value = [CACHE_NAME], key = "#config.key")
    fun getWidgetDataByConfig(
        config: WidgetConfiguration
    ): DashboardWidgetDataResultDto {
        val method = widgetDataSourceResolver.dataSourceMethodMap.toList()
            .single { (ds, _) -> ds.key == config.dataSourceKey }.second

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

        return DashboardWidgetDataResultDto(config.key, dataResult)
    }

    /**
     * This is a workaround to make caching (AOP) work internal calls
     */
    private fun self(): DashboardDataService =
        applicationContext.getBean(this::class.java)

    companion object {
        private const val CACHE_NAME = "widgetDataResults"
    }
}