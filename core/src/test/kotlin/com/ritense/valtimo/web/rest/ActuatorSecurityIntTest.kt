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

package com.ritense.valtimo.web.rest

import com.jayway.jsonpath.JsonPath.read
import com.jayway.jsonpath.PathNotFoundException
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.Base64

@TestPropertySource(properties = ["management.port=0"])
class ActuatorSecurityIntTest : SecuritySpecificEndpointIntegrationTest() {

    @Test
    fun `actuator user should have full access to health endpoint`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/actuator/health")
            .init()
            .withBasicTestUser()

        val response = mockMvc.perform(request).andReturn().response
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())

        val pingStatus = read<Any>(response.contentAsString, "$.components.ping.status")
        assertThat(pingStatus).isEqualTo("UP")
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `authenticated user should have access to health endpoint without details`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/actuator/health")
            .init()

        val response = mockMvc.perform(request).andReturn().response
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())

        assertThrows<PathNotFoundException> {
            read<Map<String, Any>>(response.contentAsString, "$.components")
        }
    }

    @Test
    fun `unauthenticated user should have access to health endpoint without details`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/actuator/health")
            .init()

        val response = mockMvc.perform(request).andReturn().response
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())

        assertThrows<PathNotFoundException> {
            read<Map<String, Any>>(response.contentAsString, "$.components")
        }
    }

    @Test
    fun `actuator user should have access to configprops endpoint`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/actuator/configprops")
            .init()
            .withBasicTestUser()

        val response = mockMvc.perform(request).andReturn().response
        assertThat(response.status).isEqualTo(HttpStatus.OK.value())

        val contexts = read<Map<String,Any>>(response.contentAsString, "$.contexts")
        assertThat(contexts).containsKey("application")
    }

    @Test
    fun `actuator user should not have access to non-actuator endpoints`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/api/v1/valtimo/version")
            .init()
            .withBasicTestUser()
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `admin user should not have access to actuator endpoints`() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/actuator/configprops")
            .init()
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

    fun MockHttpServletRequestBuilder.init() =
        this.accept(MediaType.APPLICATION_JSON)
            .with { r: MockHttpServletRequest ->
                r.remoteAddr = "8.8.8.8"
                r
            }

    fun MockHttpServletRequestBuilder.withBasicTestUser(): MockHttpServletRequestBuilder {
        val credentials = Base64.getEncoder().encodeToString("test:test".toByteArray())

        return this.header("Authorization", "Basic $credentials")
    }

}
