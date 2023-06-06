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
import java.util.UUID

open class DashboardService(
    private val dashboardRepository: DashboardRepository
) {

    @Transactional
    open fun getDashboards(): Collection<Dashboard> {
        return dashboardRepository.findAllByOrderByOrderAsc()
    }

    fun createDashboard(title: String): Dashboard {
        val order = dashboardRepository.count().toInt()
        return dashboardRepository.save(
            Dashboard(
                title = title,
                order = order
            )
        )
    }

    @Transactional
    open fun updateDashboards(dashboardUpdateDtos: List<DashboardUpdateRequestDto>): List<Dashboard> {
        dashboardUpdateDtos.forEach {
            if (!dashboardRepository.existsById(it.id)) {
                throw RuntimeException("Failed to updated dashboard. Cause dashboard with id '${it.id}' doesn't exist.")
            }
        }

        val dashboards = dashboardUpdateDtos.mapIndexed { index, dashboardUpdateDto ->
            Dashboard(
                id = dashboardUpdateDto.id,
                title = dashboardUpdateDto.title,
                order = index
            )
        }

        return dashboardRepository.saveAll(dashboards)
    }

    fun deleteDashboard(dashboardId: UUID) {
        dashboardRepository.deleteById(dashboardId)
    }
}