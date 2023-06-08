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

import com.ritense.dashboard.domain.Dashboard
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.dashboard.web.rest.dto.DashboardUpdateRequestDto
import org.springframework.transaction.annotation.Transactional

@Transactional
class DashboardService(
    private val dashboardRepository: DashboardRepository
) {

    @Transactional(readOnly = true)
    fun getDashboards(): List<Dashboard> {
        return dashboardRepository.findAllWithWidgetConfigurations()
    }

    fun createDashboard(key: String, title: String): Dashboard {
        val order = dashboardRepository.count().toInt()
        return dashboardRepository.save(
            Dashboard(
                key = key,
                title = title,
                order = order
            )
        )
    }

    fun updateDashboards(dashboardUpdateDtos: List<DashboardUpdateRequestDto>): List<Dashboard> {
        dashboardUpdateDtos.forEach {
            if (!dashboardRepository.existsById(it.key)) {
                throw RuntimeException("Failed to updated dashboard. Cause dashboard with key '${it.key}' doesn't exist.")
            }
        }

        val dashboards = dashboardUpdateDtos.mapIndexed { index, dashboardUpdateDto ->
            Dashboard(
                key = dashboardUpdateDto.key,
                title = dashboardUpdateDto.title,
                order = index
            )
        }

        dashboardRepository.deleteAll()
        return dashboardRepository.saveAll(dashboards)
    }

    fun deleteDashboard(dashboardKey: String) {
        dashboardRepository.deleteById(dashboardKey)
        updateOrder()
    }

    private fun updateOrder() {
        val dashboards = dashboardRepository.findAllByOrderByOrder()
            .mapIndexed { index, dashboard -> dashboard.copy(order = index) }
        dashboardRepository.saveAll(dashboards)
    }
}