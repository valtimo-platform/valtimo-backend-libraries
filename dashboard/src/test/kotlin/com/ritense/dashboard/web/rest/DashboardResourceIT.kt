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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.dashboard.BaseIntegrationTest
import com.ritense.dashboard.domain.WidgetConfiguration
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.service.DashboardService
import com.ritense.dashboard.web.rest.dto.DashboardCreateRequestDto
import com.ritense.dashboard.web.rest.dto.DashboardUpdateRequestDto
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationCreateRequestDto
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationUpdateRequestDto
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
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

class DashboardResourceIT : BaseIntegrationTest() {

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
        val testUser = ValtimoUser()
        testUser.firstName = "John"
        testUser.lastName = "Joe"
        whenever(userManagementService.currentUser).thenReturn(testUser)
    }

    @AfterEach
    fun afterEach() {
        widgetConfigurationRepository.deleteAll()
        dashboardRepository.deleteAll()
    }

    @Test
    fun `should get dashboards`() {
        val dashboard = dashboardService.createDashboard("Test dashboard", "Test description")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 50 }""") as ObjectNode,
                displayType = "gauge",
                order = 1
            )
        )

        mockMvc.perform(
            get("/api/v1/dashboard")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value("test_dashboard"))
            .andExpect(jsonPath("$[0].title").value("Test dashboard"))
            .andExpect(jsonPath("$[0].description").value("Test description"))
            .andExpect(jsonPath("$[0].widgetConfigurations").doesNotExist())
    }

    @Test
    fun `should get dashboard by key`() {
        val dashboard = dashboardService.createDashboard("Test dashboard", "Test description")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 50 }""") as ObjectNode,
                displayType = "gauge",
                order = 1
            )
        )

        mockMvc.perform(
            get("/api/v1/dashboard/{dashboardKey}", "test_dashboard")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("test_dashboard"))
            .andExpect(jsonPath("$.title").value("Test dashboard"))
            .andExpect(jsonPath("$.description").value("Test description"))
            .andExpect(jsonPath("$.widgetConfigurations").doesNotExist())
    }

    @Test
    fun `should create dashboard`() {
        val dashboard =
            DashboardCreateRequestDto("Test dashboard", "Test description")

        mockMvc.perform(
            post("/api/v1/dashboard")
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(dashboard))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("test_dashboard"))
            .andExpect(jsonPath("$.title").value("Test dashboard"))
            .andExpect(jsonPath("$.description").value("Test description"))
    }

    @Test
    fun `should update dashboard`() {
        val dashboard1 = dashboardService.createDashboard("First dashboard", "Test description")
        val dashboard2 = dashboardService.createDashboard("Second dashboard", "Test description")
        val updateRequest = listOf(dashboard2, dashboard1.copy(title = "Third dashboard"))
            .map { DashboardUpdateRequestDto.of(it) }

        mockMvc.perform(
            put("/api/v1/dashboard")
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(updateRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value("second_dashboard"))
            .andExpect(jsonPath("$[0].title").value("Second dashboard"))
            .andExpect(jsonPath("$[1].key").value("first_dashboard"))
            .andExpect(jsonPath("$[1].title").value("Third dashboard"))
    }

    @Test
    fun `should delete dashboard`() {
        val dashboard =
            dashboardService.createDashboard("Test dashboard", "Test description")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().createObjectNode(),
                displayType = "gauge",
                order = 1
            )
        )

        mockMvc.perform(
            delete("/api/v1/dashboard/{dashboardId}", dashboard.key)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        assertThat(dashboardRepository.existsById(dashboard.key)).isFalse
    }

    @Test
    fun `should get widget configurations`() {
        val dashboard = dashboardService.createDashboard("Test dashboard", "Test description")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 50 }""") as ObjectNode,
                displayType = "gauge",
                order = 1
            )
        )

        mockMvc.perform(
            get("/api/v1/dashboard/{dashboardKey}/widget", "test_dashboard")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].key").value("doorlooptijd"))
            .andExpect(jsonPath("$[0].dataSourceKey").value("doorlooptijd"))
            .andExpect(jsonPath("$[0].dataSourceProperties.threshold").value(50))
            .andExpect(jsonPath("$[0].displayType").value("gauge"))
    }

    @Test
    fun `should create widget configuration`() {
        dashboardService.createDashboard("Test dashboard", "Test description")
        val widgetConfiguration = WidgetConfigurationCreateRequestDto(
            title = "Doorlooptijd",
            dataSourceKey = "doorlooptijd",
            dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 50 }""") as ObjectNode,
            displayType = "gauge"
        )

        mockMvc.perform(
            post("/api/v1/dashboard/{dashboardKey}/widget", "test_dashboard")
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(widgetConfiguration))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("doorlooptijd"))
            .andExpect(jsonPath("$.dataSourceKey").value("doorlooptijd"))
            .andExpect(jsonPath("$.dataSourceProperties.threshold").value(50))
            .andExpect(jsonPath("$.displayType").value("gauge"))
    }

    @Test
    fun `should update widget configurations`() {
        val dashboard = dashboardService.createDashboard("Test dashboard", "Test description")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 50 }""") as ObjectNode,
                displayType = "gauge",
                order = 0
            )
        )
        val widgetConfigurations = listOf(
            WidgetConfigurationUpdateRequestDto(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dataSourceKey = "doorlooptijd2",
                dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 500 }""") as ObjectNode,
                displayType = "donut"
            )
        )

        mockMvc.perform(
            put("/api/v1/dashboard/{dashboardKey}/widget", "test_dashboard")
                .contentType(APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(widgetConfigurations))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].key").value("doorlooptijd"))
            .andExpect(jsonPath("$[0].title").value("Doorlooptijd"))
            .andExpect(jsonPath("$[0].dataSourceKey").value("doorlooptijd2"))
            .andExpect(jsonPath("$[0].dataSourceProperties.threshold").value(500))
            .andExpect(jsonPath("$[0].displayType").value("donut"))
    }

    @Test
    fun `should get widget configuration by id`() {
        val dashboard = dashboardService.createDashboard("Test dashboard", "Test description")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 50 }""") as ObjectNode,
                displayType = "gauge",
                order = 1
            )
        )

        mockMvc.perform(
            get("/api/v1/dashboard/{dashboardKey}/widget/{widgetKey}", "test_dashboard", "doorlooptijd")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("doorlooptijd"))
            .andExpect(jsonPath("$.dataSourceKey").value("doorlooptijd"))
            .andExpect(jsonPath("$.dataSourceProperties.threshold").value(50))
            .andExpect(jsonPath("$.displayType").value("gauge"))
    }

    @Test
    fun `should delete widget configuration`() {
        val dashboard = dashboardService.createDashboard("Test dashboard", "Test description")
        widgetConfigurationRepository.save(
            WidgetConfiguration(
                key = "doorlooptijd",
                title = "Doorlooptijd",
                dashboard = dashboard,
                dataSourceKey = "doorlooptijd",
                dataSourceProperties = jacksonObjectMapper().readTree("""{ "threshold": 50 }""") as ObjectNode,
                displayType = "gauge",
                order = 1
            )
        )

        mockMvc.perform(
            delete("/api/v1/dashboard/{dashboardKey}/widget/{widgetKey}", "test_dashboard", "doorlooptijd")
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }
}
