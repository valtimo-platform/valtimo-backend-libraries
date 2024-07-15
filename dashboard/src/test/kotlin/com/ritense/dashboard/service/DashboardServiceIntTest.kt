/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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