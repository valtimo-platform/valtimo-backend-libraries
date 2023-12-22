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

package com.ritense.valtimo.web.rest

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.Base64

@TestPropertySource(properties = ["management.port=0"])
class ActuatorSecurityIntTest(
    private val context: WebApplicationContext
) : SecuritySpecificEndpointIntegrationTest() {

    @BeforeEach
    fun setUp() {
        //context.getBean(HealthEndpoint::class.java).enabled = true
    }

    @Test
    fun `actuator user should have access to actuator endpoints`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/actuator/health")
        val credentials = Base64.getEncoder().encodeToString("test:test".toByteArray())
        request.accept(MediaType.APPLICATION_JSON)
        request.header("Authorization", "Basic $credentials")
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    fun `actuator user should not have access to non-actuator endpoints`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/api/v1/valtimo/version")
        val credentials = Base64.getEncoder().encodeToString("test:test".toByteArray())
        request.accept(MediaType.APPLICATION_JSON)
        request.header("Authorization", "Basic $credentials")
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `admin user should not have access to actuator endpoints`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/actuator/health")
        val credentials = Base64.getEncoder().encodeToString("test:test".toByteArray())
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }
}
