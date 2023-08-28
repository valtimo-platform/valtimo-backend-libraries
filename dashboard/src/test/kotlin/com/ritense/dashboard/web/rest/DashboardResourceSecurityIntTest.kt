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

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.security.test.context.support.WithMockUser

class DashboardResourceSecurityIntTest : SecuritySpecificEndpointIntegrationTest() {

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should have access to retrieve dashboards method with role_user`() {
        assertHttpStatus(GET, "/api/v1/dashboard", OK)
    }
}