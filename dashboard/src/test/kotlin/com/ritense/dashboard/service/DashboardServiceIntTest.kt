package com.ritense.dashboard.service

import com.ritense.dashboard.BaseIntegrationTest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser

class DashboardServiceIntTest : BaseIntegrationTest() {
    @Nested
    @SpringBootTest(properties = ["valtimo.authorization.dashboard.enabled=false"])
    inner class Disabled @Autowired constructor(
        private val dashboardService: DashboardService
    ) {
        @Test
        @WithMockUser(username = TEST_USER, authorities = [AuthoritiesConstants.USER])
        fun `should not use access control when property is disabled`() {
            val dashboards = dashboardService.getDashboards()
            Assertions.assertThat(dashboards).hasSize(2)
        }
    }

    @Nested
    @SpringBootTest(properties = ["valtimo.authorization.dashboard.enabled=true"])
    inner class Enabled @Autowired constructor(
        private val dashboardService: DashboardService
    ) {
        @Test
        @WithMockUser(username = TEST_USER, authorities = [AuthoritiesConstants.USER])
        fun `should use access control when property is enabled`() {
            val dashboards = dashboardService.getDashboards()
            Assertions.assertThat(dashboards).hasSize(1)
        }
    }

    @Nested
    @SpringBootTest
    inner class NotSet @Autowired constructor(
        private val dashboardService: DashboardService
    ) {
        @Test
        @WithMockUser(username = TEST_USER, authorities = [AuthoritiesConstants.USER])
        fun `should not use access control when property is not set`() {
            val dashboards = dashboardService.getDashboards()
            Assertions.assertThat(dashboards).hasSize(2)
        }
    }

    companion object {
        private const val TEST_USER = "user@valtimo.nl"
    }
}