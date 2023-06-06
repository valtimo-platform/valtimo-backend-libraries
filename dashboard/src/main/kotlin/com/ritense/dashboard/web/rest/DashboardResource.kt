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

package com.ritense.dashboard.web.rest

import com.ritense.dashboard.service.DashboardService
import com.ritense.dashboard.web.rest.dto.DashboardCreateRequestDto
import com.ritense.dashboard.web.rest.dto.DashboardResponseDto
import com.ritense.dashboard.web.rest.dto.DashboardUpdateRequestDto
import com.ritense.dashboard.web.rest.dto.DashboardWithWidgetConfigurationsResponseDto
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID

@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class DashboardResource(
    private val dashboardService: DashboardService,
) {

    @GetMapping("/v1/dashboard")
    fun getDashboards(): ResponseEntity<List<DashboardWithWidgetConfigurationsResponseDto>> {
        val dashboardResponseDtos = dashboardService.getDashboards()
            .map { DashboardWithWidgetConfigurationsResponseDto.of(it) }
        return ResponseEntity.ok(dashboardResponseDtos)
    }

    @PostMapping("/v1/dashboard")
    fun createDashboard(
        @RequestBody dashboardDto: DashboardCreateRequestDto
    ): ResponseEntity<DashboardResponseDto> {
        val dashboard = dashboardService.createDashboard(
            dashboardDto.title,
        )
        return ResponseEntity.ok(DashboardResponseDto.of(dashboard))
    }

    @PutMapping("/v1/dashboard")
    fun editDashboard(
        @PathVariable(name = "dashboardId") dashboardId: UUID,
        @RequestBody dashboardUpdateRequestDtos: List<DashboardUpdateRequestDto>
    ): ResponseEntity<List<DashboardResponseDto>> {
        val dashboardResponseDtos = dashboardService.updateDashboards(dashboardUpdateRequestDtos)
            .map { DashboardResponseDto.of(it) }
        return ResponseEntity.ok(dashboardResponseDtos)
    }

    @DeleteMapping("/v1/dashboard/{dashboardId}")
    fun deleteDashboard(
        @PathVariable(name = "dashboardId") dashboardId: UUID
    ): ResponseEntity<Unit> {
        dashboardService.deleteDashboard(dashboardId)
        return ResponseEntity.noContent().build()
    }
}
