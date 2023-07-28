package com.ritense.dashboard.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.dashboard.BaseIntegrationTest
import com.ritense.dashboard.TestDataSourceProperties
import com.ritense.dashboard.datasource.dto.DashboardWidgetListDto
import com.ritense.dashboard.datasource.dto.DashboardWidgetSingleDto
import com.ritense.dashboard.domain.WidgetConfiguration
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean

class DashboardDataServiceIntTest @Autowired constructor(
    private val dashboardDataService: DashboardDataService,
    private val objectMapper: ObjectMapper,
    @SpyBean private val widgetConfigurationRepository: WidgetConfigurationRepository
): BaseIntegrationTest() {

    @Test
    fun `should get data from test datasource`() {
        val dashboardKey = "test"
        whenever(widgetConfigurationRepository.findAllByDashboardKey(dashboardKey)).thenReturn(
            listOf(
                WidgetConfiguration("single-test", "", mock(), "test-key-single",
                    objectMapper.valueToTree(TestDataSourceProperties("xyz")),
                    "", 0),
                WidgetConfiguration("multi-test", "", mock(), "test-key-multi",
                    objectMapper.createObjectNode() , "", 1)
            )
        )
        val widgetData = dashboardDataService.getWidgetDataForDashboard(dashboardKey)
        assertThat(widgetData).hasSize(2)
        assertThat(widgetData[0].result).isInstanceOf(DashboardWidgetSingleDto::class.java)
        assertThat(widgetData[1].result).isInstanceOf(DashboardWidgetListDto::class.java)
    }
}