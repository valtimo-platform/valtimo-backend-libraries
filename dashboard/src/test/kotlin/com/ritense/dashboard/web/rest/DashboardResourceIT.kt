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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.dashboard.BaseIntegrationTest
import com.ritense.dashboard.domain.WidgetConfiguration
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.service.DashboardService
import com.ritense.dashboard.web.rest.dto.DashboardCreateRequestDto
import com.ritense.dashboard.web.rest.dto.DashboardUpdateRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

internal class DashboardResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var dashboardService: DashboardService

    @Autowired
    lateinit var dashboardRepository: DashboardRepository

    @Autowired
    lateinit var widgetConfigurationRepository: WidgetConfigurationRepository

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @Test
    fun `should create dashboard`() {
        val dashboard = DashboardCreateRequestDto(title = "Test dashboard")

        mockMvc.perform(
            post("/api/v1/dashboard")
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(dashboard))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.title").value("anId"))
    }

    @Test
    fun `should get dashboards`() {
        val dashboard = dashboardService.createDashboard("Test dashboard")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().createObjectNode(),
                displayType = "gauge",
                order = 1
            )
        )

        mockMvc.perform(
            get("/api/v1/dashboard")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").isNotEmpty)
            .andExpect(jsonPath("$.content[0].title").value("Test dashboard"))
            .andExpect(jsonPath("$.content[0].widgetConfigurations.size()").value(1))
            .andExpect(jsonPath("$.content[0].widgetConfigurations[0].id").isNotEmpty)
            .andExpect(jsonPath("$.content[0].widgetConfigurations[0].dashboard").value(dashboard))
            .andExpect(jsonPath("$.content[0].widgetConfigurations[0].dataSourceKey").value("doorlooptijd"))
            .andExpect(jsonPath("$.content[0].widgetConfigurations[0].dataSourceProperties").isNotEmpty)
            .andExpect(jsonPath("$.content[0].widgetConfigurations[0].displayType").value("gauge"))
            .andExpect(jsonPath("$.content[0].widgetConfigurations[0].order").value(1))
    }

    @Test
    fun `should update dashboard`() {
        val dashboard1 = dashboardService.createDashboard("First dashboard")
        val dashboard2 = dashboardService.createDashboard("Second dashboard")
        val updateRequest = listOf(dashboard2, dashboard1.copy(title = "Third dashboard"))
            .map { DashboardUpdateRequestDto.of(it) }

        mockMvc.perform(
            put("/api/v1/dashboard")
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(updateRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].title").value("Second dashboard"))
            .andExpect(jsonPath("$.content[1].title").value("Third dashboard"))
    }

    @Test
    fun `should delete dashboard`() {
        val dashboard = dashboardService.createDashboard("Test dashboard")

        mockMvc.perform(
            delete("/api/v1/dashboard/{dashboardId}", dashboard.id)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        assertThat(dashboardRepository.existsById(dashboard.id)).isFalse
    }
}
