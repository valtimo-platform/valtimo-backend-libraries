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

import com.ritense.valtimo.contract.authentication.UserManagementService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.HttpClientErrorException.Forbidden
import org.springframework.web.client.RestTemplate


@ExtendWith(SpringExtension::class)
@Tag("security")
class CamundaCockpitSecurityIntTest {

    @Nested
    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = ["valtimo.security.whitelist.hosts=localhost"])
    inner class LocalhostWhiteListed @Autowired constructor(
        @LocalServerPort private val serverPort: Int,
        @MockBean private val userManagementService: UserManagementService,
    ) {

        @Test
        fun `localhost should be whitelisted to access camunda cockpit endpoints`() {
            val restTemplate = RestTemplate()
            val response = restTemplate.getForEntity("http://127.0.0.1:$serverPort/camunda/api/engine/engine/", String::class.java)

            assertThat(response.statusCode.value()).isEqualTo(200)
            assertThat(response.body).isEqualTo("""[{"name":"default"}]""")
        }
    }
    @Nested
    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = ["valtimo.security.whitelist.hosts=8.8.8.8"])
    inner class GoogleWhiteListed @Autowired constructor(
        @LocalServerPort private val serverPort: Int,
        @MockBean private val userManagementService: UserManagementService,
    ) {

        @Test
        fun `localhost should not be whitelisted to access camunda cockpit endpoints`() {
            val restTemplate = RestTemplate()
            val forbidden = assertThrows<Forbidden> {
                restTemplate.getForEntity("http://127.0.0.1:$serverPort/camunda/api/engine/engine/", String::class.java)
            }

            assertThat(forbidden.statusCode.value()).isEqualTo(403)
        }
    }
}
