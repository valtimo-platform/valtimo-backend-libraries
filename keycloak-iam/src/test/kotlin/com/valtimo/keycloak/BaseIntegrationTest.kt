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

package com.valtimo.keycloak

import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.oauth2.client.registration.ClientRegistrations
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import java.net.URI
import kotlin.Int.Companion.MAX_VALUE
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.isAccessible

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @MockBean
    lateinit var mailSender: MailSender

    @MockBean
    lateinit var userManagementService: UserManagementService

    companion object {

        lateinit var mockServer: MockRestServiceServer
        lateinit var keycloakRestTemplate: RestTemplate

        @JvmStatic
        @BeforeAll
        fun setUp() {
            val restTemplateField = ClientRegistrations::class.staticProperties.single { it.name == "rest" }
            restTemplateField.isAccessible = true
            keycloakRestTemplate = restTemplateField.get() as RestTemplate
            mockServer = MockRestServiceServer.createServer(keycloakRestTemplate)
            mockServer.expect(
                ExpectedCount.between(0, MAX_VALUE),
                requestTo(URI("https://ritense.com/auth/realms/valtimo/.well-known/openid-configuration"))
            )
                .andExpect(method(GET))
                .andRespond(
                    withStatus(OK)
                        .contentType(APPLICATION_JSON)
                        .body(readFileAsString("/data/get-openid-configuration.json"))
                )
        }

        fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName).readText(Charsets.UTF_8)
    }
}
