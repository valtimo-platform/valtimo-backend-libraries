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
import com.ritense.dashboard.web.rest.dto.DashboardCreateRequestDto
import com.ritense.dashboard.web.rest.dto.DashboardUpdateRequestDto
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationCreateRequestDto
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationUpdateRequestDto
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.security.test.context.support.WithMockUser

class AdminDashboardResourceSecurityIntTest : SecuritySpecificEndpointIntegrationTest() {

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to retrieve dashboards method with role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard", OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to retrieve dashboards method without role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard", FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to retrieve dashboard by id method with role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/1", INTERNAL_SERVER_ERROR)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to retrieve dashboard by id method without role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/1", FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to create dashboard method with role_admin`() {
        val content = DashboardCreateRequestDto("title", "description")
        assertHttpStatus(POST, "/api/management/v1/dashboard", content, INTERNAL_SERVER_ERROR)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to create dashboard method without role_admin`() {
        val content = DashboardCreateRequestDto("title", "description")
        assertHttpStatus(POST, "/api/management/v1/dashboard", content, FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to update dashboard with role_admin`() {
        val content = DashboardUpdateRequestDto("key", "title", "description")
        assertHttpStatus(PUT, "/api/management/v1/dashboard", listOf(content), INTERNAL_SERVER_ERROR)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to update dashboard method without role_admin`() {
        val content = DashboardUpdateRequestDto("key", "title", "description")
        assertHttpStatus(PUT, "/api/management/v1/dashboard", listOf(content), FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to delete dashboard method with role_admin`() {
        assertHttpStatus(DELETE, "/api/management/v1/dashboard/1", INTERNAL_SERVER_ERROR)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to delete dashboard method without role_admin`() {
        assertHttpStatus(DELETE, "/api/management/v1/dashboard/1", FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to get widget configuration method with role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/1/widget-configuration", OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to get widget configuration method without role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/1/widget-configuration", FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to create widget configuration method with role_admin`() {
        val properties = jacksonObjectMapper().createObjectNode()
        val content = WidgetConfigurationCreateRequestDto("key", "dataSourceKey", "displayType", properties, properties)
        assertHttpStatus(POST, "/api/management/v1/dashboard/1/widget-configuration", content, INTERNAL_SERVER_ERROR)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to create widget configuration method without role_admin`() {
        val properties = jacksonObjectMapper().createObjectNode()
        val content = WidgetConfigurationCreateRequestDto("title", "dataSourceKey", "displayType", properties, properties)
        assertHttpStatus(POST, "/api/management/v1/dashboard/1/widget-configuration", content, FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to update widget configuration method with role_admin`() {
        val properties = jacksonObjectMapper().createObjectNode()
        val content = WidgetConfigurationUpdateRequestDto("key", "title", "dataSourceKey", "displayType", properties, properties)
        assertHttpStatus(PUT, "/api/management/v1/dashboard/1/widget-configuration", listOf(content), INTERNAL_SERVER_ERROR)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to update widget configuration method without role_admin`() {
        val properties = jacksonObjectMapper().createObjectNode()
        val content = WidgetConfigurationUpdateRequestDto("key", "title", "dataSourceKey", "displayType", properties, properties)
        assertHttpStatus(PUT, "/api/management/v1/dashboard/1/widget-configuration", listOf(content), FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to get widget configuration by key method with role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/1/widget-configuration/1", INTERNAL_SERVER_ERROR)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to get widget configuration by key method without role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/1/widget-configuration/1", FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to delete widget configuration method with role_admin`() {
        assertHttpStatus(DELETE, "/api/management/v1/dashboard/1/widget-configuration/1", NO_CONTENT)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to delete widget configuration method without role_admin`() {
        assertHttpStatus(DELETE, "/api/management/v1/dashboard/1/widget-configuration/1", FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to retrieve widget data sources method with role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/widget-data-sources", OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to retrieve widget data sources method without role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/dashboard/widget-data-sources", FORBIDDEN)
    }

}