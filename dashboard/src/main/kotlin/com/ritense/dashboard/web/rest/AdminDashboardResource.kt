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
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationCreateRequestDto
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationResponseDto
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationUpdateRequestDto
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class AdminDashboardResource(
    private val dashboardService: DashboardService,
) {

    @GetMapping("/v1/dashboard")
    fun getDashboards(): ResponseEntity<List<DashboardResponseDto>> {
        val dashboardResponseDtos = dashboardService.getDashboards()
            .map { DashboardResponseDto.of(it) }
        return ResponseEntity.ok(dashboardResponseDtos)
    }

    @GetMapping("/v1/dashboard/{dashboardKey}")
    fun getDashboard(
        @PathVariable(name = "dashboardKey") dashboardKey: String
    ): ResponseEntity<DashboardResponseDto> {
        val dashboard = dashboardService.getDashboard(dashboardKey)
        return ResponseEntity.ok(DashboardResponseDto.of(dashboard))
    }

    @PostMapping("/v1/dashboard")
    fun createDashboard(
        @RequestBody dashboardDto: DashboardCreateRequestDto
    ): ResponseEntity<DashboardResponseDto> {
        val dashboard = dashboardService.createDashboard(
            dashboardDto.title,
            dashboardDto.description,
        )
        return ResponseEntity.ok(DashboardResponseDto.of(dashboard))
    }

    @PutMapping("/v1/dashboard")
    fun editDashboard(
        @RequestBody dashboardUpdateRequestDtos: List<DashboardUpdateRequestDto>
    ): ResponseEntity<List<DashboardResponseDto>> {
        val dashboardResponseDtos = dashboardService.updateDashboards(dashboardUpdateRequestDtos)
            .map { DashboardResponseDto.of(it) }
        return ResponseEntity.ok(dashboardResponseDtos)
    }

    @DeleteMapping("/v1/dashboard/{dashboardKey}")
    fun deleteDashboard(
        @PathVariable(name = "dashboardKey") dashboardKey: String
    ): ResponseEntity<Unit> {
        dashboardService.deleteDashboard(dashboardKey)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/v1/dashboard/{dashboardKey}/widget-configuration")
    fun getWidgetConfigurations(
        @PathVariable(name = "dashboardKey") dashboardKey: String
    ): ResponseEntity<List<WidgetConfigurationResponseDto>> {
        val widgetDtos = dashboardService.getWidgetConfigurations(dashboardKey)
            .map { WidgetConfigurationResponseDto.of(it) }
        return ResponseEntity.ok(widgetDtos)
    }

    @PostMapping("/v1/dashboard/{dashboardKey}/widget-configuration")
    fun createWidgetConfigurations(
        @PathVariable(name = "dashboardKey") dashboardKey: String,
        @RequestBody widgetDto: WidgetConfigurationCreateRequestDto,
    ): ResponseEntity<WidgetConfigurationResponseDto> {
        val widget = dashboardService.createWidgetConfiguration(
            dashboardKey,
            widgetDto.title,
            widgetDto.dataSourceKey,
            widgetDto.displayType,
            widgetDto.dataSourceProperties,
        )
        return ResponseEntity.ok(WidgetConfigurationResponseDto.of(widget))
    }

    @PutMapping("/v1/dashboard/{dashboardKey}/widget-configuration")
    fun editWidgetConfigurations(
        @PathVariable(name = "dashboardKey") dashboardKey: String,
        @RequestBody widgetUpdateRequestDtos: List<WidgetConfigurationUpdateRequestDto>
    ): ResponseEntity<List<WidgetConfigurationResponseDto>> {
        val widgetResponseDtos = dashboardService.updateWidgetConfigurations(dashboardKey, widgetUpdateRequestDtos)
            .map { WidgetConfigurationResponseDto.of(it) }
        return ResponseEntity.ok(widgetResponseDtos)
    }

    @GetMapping("/v1/dashboard/{dashboardKey}/widget-configuration/{widgetKey}")
    fun getWidgetConfigurations(
        @PathVariable(name = "dashboardKey") dashboardKey: String,
        @PathVariable(name = "widgetKey") widgetKey: String,
    ): ResponseEntity<WidgetConfigurationResponseDto> {
        val widget = dashboardService.getWidgetConfiguration(dashboardKey, widgetKey)
        return ResponseEntity.ok(WidgetConfigurationResponseDto.of(widget))
    }

    @DeleteMapping("/v1/dashboard/{dashboardKey}/widget-configuration/{widgetKey}")
    fun deleteWidgetConfigurations(
        @PathVariable(name = "dashboardKey") dashboardKey: String,
        @PathVariable(name = "widgetKey") widgetKey: String,
    ): ResponseEntity<Unit> {
        dashboardService.deleteWidgetConfiguration(dashboardKey, widgetKey)
        return ResponseEntity.noContent().build()
    }
}
