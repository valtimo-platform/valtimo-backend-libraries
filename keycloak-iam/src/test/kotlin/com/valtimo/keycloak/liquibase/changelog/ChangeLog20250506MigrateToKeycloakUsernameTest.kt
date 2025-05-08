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

package com.valtimo.keycloak.liquibase.changelog

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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.env.MockEnvironment
import java.sql.PreparedStatement
import java.sql.ResultSet

internal class ChangeLog20250506MigrateToKeycloakUsernameTest {

    lateinit var server: MockWebServer

    lateinit var changeLog: ChangeLog20250506MigrateToKeycloakUsername
    lateinit var environment: MockEnvironment


    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockKeycloakApiServer()
        server.start()

        environment = MockEnvironment().apply {
            this.setProperty("keycloak.auth-server-url", server.url("/").toString())
            this.setProperty("keycloak.realm", "example-realm")
            this.setProperty("keycloak.resource", "example-resource")
            this.setProperty("keycloak.credentials.secret", "example-secret")
        }

        ChangeLog20250506MigrateToKeycloakUsername().postProcessEnvironment(environment, mock())

        changeLog = ChangeLog20250506MigrateToKeycloakUsername()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should execute changelog for USERNAME`() {
        val database = mock<Database>()
        val connection = mock<JdbcConnection>(defaultAnswer = RETURNS_DEEP_STUBS)
        val resultSet = mock<ResultSet>()
        val updateTaskTable = mock<PreparedStatement>()
        whenever(database.connection).thenReturn(connection)
        whenever(connection.prepareStatement("SELECT id_,assignee_ FROM act_ru_task").executeQuery())
            .thenReturn(resultSet)
        whenever(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false)
        whenever(resultSet.getString("id_")).thenReturn("my-task-id-1")
        whenever(resultSet.getString("assignee_")).thenReturn("user@ritense.com")
        whenever(connection.prepareStatement("UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?"))
            .thenReturn(updateTaskTable)
        whenever(resultSet.getBoolean(1)).thenReturn(true)
        whenever(
            connection.prepareStatement(
                """
            SELECT EXISTS (
                SELECT 1
                FROM information_schema.tables
                WHERE table_schema = 'null'
                  AND table_name = 'act_ru_task'
            );
        """.trimIndent()
            ).executeQuery()
        ).thenReturn(resultSet)

        changeLog.execute(database)

        verify(updateTaskTable).setString(1, "user-name-1")
        verify(updateTaskTable).setString(2, "my-task-id-1")
    }

    @Test
    fun `should set assignee to null when user cannot be found by email`() {
        val database = mock<Database>()
        val connection = mock<JdbcConnection>(defaultAnswer = RETURNS_DEEP_STUBS)
        val resultSet = mock<ResultSet>()
        val updateTaskTable = mock<PreparedStatement>()
        whenever(database.connection).thenReturn(connection)
        whenever(connection.prepareStatement("SELECT id_,assignee_ FROM act_ru_task").executeQuery())
            .thenReturn(resultSet)
        whenever(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false)
        whenever(resultSet.getString("id_")).thenReturn("my-task-id-1")
        whenever(resultSet.getString("assignee_")).thenReturn("notfound@ritense.com")
        whenever(connection.prepareStatement("UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?"))
            .thenReturn(updateTaskTable)
        whenever(resultSet.getBoolean(1)).thenReturn(true)
        whenever(
            connection.prepareStatement(
                """
            SELECT EXISTS (
                SELECT 1
                FROM information_schema.tables
                WHERE table_schema = 'null'
                  AND table_name = 'act_ru_task'
            );
        """.trimIndent()
            ).executeQuery()
        ).thenReturn(resultSet)

        changeLog.execute(database)

        verify(updateTaskTable).setString(1, null)
        verify(updateTaskTable).setString(2, "my-task-id-1")
    }

    @Test
    fun `should not update task on a keycloak error`() {
        val database = mock<Database>()
        val connection = mock<JdbcConnection>(defaultAnswer = RETURNS_DEEP_STUBS)
        val resultSet = mock<ResultSet>()
        whenever(database.connection).thenReturn(connection)
        whenever(connection.prepareStatement("SELECT id_,assignee_ FROM act_ru_task").executeQuery())
            .thenReturn(resultSet)
        whenever(resultSet.next()).thenReturn(true).thenReturn(false)
        whenever(resultSet.getString("id_")).thenReturn("my-task-id-1")
        whenever(resultSet.getString("assignee_")).thenReturn("error@ritense.com")

        changeLog.execute(database)

        verify(connection, never()).prepareStatement("UPDATE act_ru_task SET assignee_ = ? WHERE id_ = ?")
    }


    private fun setupMockKeycloakApiServer() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.requestLine) {
                    "POST /realms/example-realm/protocol/openid-connect/token HTTP/1.1" -> handleTokenRequest()
                    "GET /admin/realms/example-realm/users?email=user%40ritense.com&exact=true HTTP/1.1" -> handleUserSearchRequest()
                    "GET /admin/realms/example-realm/users?email=notfound%40ritense.com&exact=true HTTP/1.1" -> handleUserSearchRequestEmpty()
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
                    "username": "user-name-1",
                    "email": "user@ritense.com"
                }
            ]
        """.trimIndent()
        return mockResponse(body)
    }

    private fun handleUserSearchRequestEmpty(): MockResponse {
        return mockResponse("""[]""".trimIndent())
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