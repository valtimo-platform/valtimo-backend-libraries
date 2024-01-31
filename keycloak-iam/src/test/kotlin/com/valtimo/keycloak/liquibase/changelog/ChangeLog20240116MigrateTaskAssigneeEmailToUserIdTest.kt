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

package com.valtimo.keycloak.liquibase.changelog

import java.sql.PreparedStatement
import java.sql.ResultSet
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.env.ConfigurableEnvironment

internal class ChangeLog20240116MigrateTaskAssigneeEmailToUserIdTest {

    lateinit var server: MockWebServer

    lateinit var changeLog: ChangeLog20240116MigrateTaskAssigneeEmailToUserId

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockKeycloakApiServer()
        server.start()

        val configurableEnvironment: ConfigurableEnvironment = mock()

        ChangeLog20240116MigrateTaskAssigneeEmailToUserId().postProcessEnvironment(configurableEnvironment, mock())
        whenever(configurableEnvironment.getProperty("keycloak.auth-server-url")).thenReturn(server.url("/").toString())
        whenever(configurableEnvironment.getProperty("keycloak.realm")).thenReturn("example-realm")
        whenever(configurableEnvironment.getProperty("keycloak.resource")).thenReturn("example-resource")
        whenever(configurableEnvironment.getProperty("keycloak.credentials.secret")).thenReturn("example-secret")

        changeLog = ChangeLog20240116MigrateTaskAssigneeEmailToUserId()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should execute changelog`() {
        val database = mock<Database>()
        val connection = mock<JdbcConnection>(defaultAnswer = RETURNS_DEEP_STUBS)
        val resultSet = mock<ResultSet>()
        val updateTaskTable = mock<PreparedStatement>()
        whenever(database.connection).thenReturn(connection)
        whenever(connection.prepareStatement("SELECT id_,assignee_ FROM act_ru_task").executeQuery())
            .thenReturn(resultSet)
        whenever(resultSet.next()).thenReturn(true).thenReturn(false)
        whenever(resultSet.getString("id_")).thenReturn("my-task-id-1")
        whenever(resultSet.getString("assignee_")).thenReturn("user@ritense.com")
        whenever(connection.prepareStatement("UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?"))
            .thenReturn(updateTaskTable)

        changeLog.execute(database)

        verify(updateTaskTable).setString(1, "user-id-1")
        verify(updateTaskTable).setString(2, "my-task-id-1")
    }


    private fun setupMockKeycloakApiServer() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.requestLine) {
                    "POST /realms/example-realm/protocol/openid-connect/token HTTP/1.1" -> handleTokenRequest()
                    "GET /admin/realms/example-realm/users?email=user%40ritense.com&first=0&max=1&enabled=true&briefRepresentation=true HTTP/1.1" -> handleUserSearchRequest()
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }

    private fun handleUserSearchRequest(): MockResponse {
        val body = """
            [
                {
                    "id": "user-id-1",
                    "email": "user@ritense.com"
                }
            ]
        """.trimIndent()
        return mockResponse(body)
    }

    private fun handleTokenRequest(): MockResponse {
        val body = """
            {
                "access_token": "eyJ",
                "expires_in": 300,
                "refresh_expires_in": 0,
                "token_type": "Bearer",
                "not-before-policy": 0,
                "scope": "profile email"
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

}