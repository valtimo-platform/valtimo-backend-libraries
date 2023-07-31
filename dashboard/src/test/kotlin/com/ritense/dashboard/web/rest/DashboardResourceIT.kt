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
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.service.DashboardService
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
                displayTypeProperties = jacksonObjectMapper().readTree("""{ "useKpi": true }""") as ObjectNode,
                displayType = "number",
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
            .andExpect(jsonPath("$[0].widgets", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].widgets[0].key").value("doorlooptijd"))
            .andExpect(jsonPath("$[0].widgets[0].title").value("Doorlooptijd"))
            .andExpect(jsonPath("$[0].widgets[0].displayType").value("number"))
            .andExpect(jsonPath("$[0].widgets[0].displayTypeProperties.useKpi").value(true))
    }
}