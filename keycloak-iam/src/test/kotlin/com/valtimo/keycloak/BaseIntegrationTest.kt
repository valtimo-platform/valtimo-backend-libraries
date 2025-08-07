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
import com.ritense.valtimo.service.ProcessDefinitionCaseDefinitionLinker
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @MockitoBean
    lateinit var processDefinitionCaseDefinitionLinker: ProcessDefinitionCaseDefinitionLinker

    @MockitoBean
    lateinit var mailSender: MailSender

    @MockitoBean
    lateinit var userManagementService: UserManagementService

    companion object {

        lateinit var server: MockWebServer

        @JvmStatic
        @BeforeAll
        fun setUp() {
            server = MockWebServer()
            setupMockKeycloakApiServer()
            server.start(port = 49152)
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            server.shutdown()
        }

        private fun setupMockKeycloakApiServer() {
            val dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val response = when (request.requestLine) {
                        "GET /auth/realms/valtimo/.well-known/openid-configuration HTTP/1.1" -> mockResponseFromFile("/data/get-openid-configuration.json")
                        "GET /auth/admin/serverinfo HTTP/1.1" -> mockResponseFromFile("/data/get-server-info.json")
                        "POST /auth/realms/valtimo/protocol/openid-connect/token HTTP/1.1" -> mockResponseFromFile("/data/grant-token-response.json")
                        else -> MockResponse().setResponseCode(404)
                    }
                    return response
                }
            }
            server.dispatcher = dispatcher
        }

        private fun mockResponseFromFile(fileName: String) =
            mockResponse(readFileAsString(fileName))

        private fun mockResponse(response: String) = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(response)

        fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName).readText(Charsets.UTF_8)
    }
}
