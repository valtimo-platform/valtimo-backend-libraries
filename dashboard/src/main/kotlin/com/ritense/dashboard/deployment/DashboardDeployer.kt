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

package com.ritense.dashboard.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.dashboard.domain.Dashboard
import com.ritense.dashboard.domain.WidgetConfiguration
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

class DashboardDeployer(
    private val objectMapper: ObjectMapper,
    private val dashboardRepository: DashboardRepository,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
): ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.dashboard.json"

    override fun before() {
        if (clearTables) {
            dashboardRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<DashboardChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.dashboards,
                key = KEY,
                deploy = { deploy(changeset.dashboards) }
            )
        )
    }

    fun deploy(dashboards: List<DashboardDto>) {
        val dashboardsToSave = dashboards.map {
            createDashboard(it)
        }

        dashboardRepository.saveAll(dashboardsToSave)
    }

    fun createDashboard(dashboardDto: DashboardDto): Dashboard {

        val dashboardWithOrder = dashboardRepository.findByOrder(dashboardDto.order)
        if (dashboardWithOrder != null && dashboardWithOrder.key != dashboardDto.key) {
            throw DeploymentFailedException("A dashboard with order ${dashboardDto.order} already exists.")
        }

        val widgetConfigurations = mutableListOf<WidgetConfiguration>()
        val dashboard = Dashboard(
            key = dashboardDto.key,
            title = dashboardDto.title,
            description = dashboardDto.description,
            widgetConfigurations = widgetConfigurations,
            order = dashboardDto.order,
            createdBy = "auto-deployed"
        )

        dashboardDto.widgetConfigurations.forEachIndexed { index, value ->
            widgetConfigurations.add(createWidget(value, dashboard, index))
        }

        return dashboard
    }

    fun createWidget(
        widgetConfigurationDto: WidgetConfigurationDto,
        dashboard: Dashboard,
        order: Int
    ): WidgetConfiguration {
        return WidgetConfiguration(
            key = widgetConfigurationDto.key,
            title = widgetConfigurationDto.title,
            dataSourceKey = widgetConfigurationDto.dataSourceKey,
            displayType = widgetConfigurationDto.displayType,
            displayTypeProperties = widgetConfigurationDto.displayTypeProperties,
            dataSourceProperties = widgetConfigurationDto.dataSourceProperties,
            dashboard = dashboard,
            order = order
        )
    }

    companion object {
        private const val KEY = "dashboard"
    }
}