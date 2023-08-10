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

package com.ritense.authorization.web.rest

import com.ritense.authorization.web.request.SearchPermissionsRequest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.utils.TestUtil.convertObjectToJsonBytes
import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request

class PermissionManagementResourceSecurityIntTest : SecuritySpecificEndpointIntegrationTest() {

    @BeforeEach
    fun setUp() {
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to search permissions method with role_admin`() {
        val request = request(POST, "/api/management/v1/permissions/search")
        request.content(convertObjectToJsonBytes(SearchPermissionsRequest(listOf("ROLE_ADMIN"))))
        request.contentType(APPLICATION_JSON)
        request.accept(APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to search permissions method without role_admin`() {
        val request = request(POST, "/api/management/v1/permissions/search")
        request.content(convertObjectToJsonBytes(SearchPermissionsRequest(listOf("ROLE_ADMIN"))))
        request.contentType(APPLICATION_JSON)
        request.accept(APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }
}