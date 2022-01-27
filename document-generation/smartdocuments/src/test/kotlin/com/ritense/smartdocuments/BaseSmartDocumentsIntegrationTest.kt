/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.smartdocuments

import com.nhaarman.mockitokotlin2.whenever
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorService
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import java.util.Optional
import java.util.UUID
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

class BaseSmartDocumentsIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var smartDocumentsConnector: Connector

    @Autowired
    lateinit var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var connectorTypeInstanceRepository: ConnectorTypeInstanceRepository

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        startMockServer()
        setupConnector()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    fun mockUser(
        id: String = UUID.randomUUID().toString(),
        email: String = "john.doe@valtimo.nl",
        lastName: String = "Doe"
    ): ManageableUser {
        val user = ValtimoUser()
        user.id = id
        user.email = email
        user.lastName = lastName
        whenever(currentUserService.currentUser).thenReturn(user)
        whenever(userManagementService.findByEmail(email)).thenReturn(Optional.of(user))
        return user
    }

    fun startMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path?.substringBefore('?')) {
                    "/wsxmldeposit/deposit/unattended" -> when (request.method) {
                        "POST" -> mockResponseFromFile("/data/post-generate-document.json")
                        else -> MockResponse().setResponseCode(404)
                    }
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server = MockWebServer()
        server.dispatcher = dispatcher
        server.start()
    }

    private fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(readFileAsString(fileName))
    }

    private fun setupConnector() {
        smartDocumentsConnectorProperties.url = server.url("/").toString()
        smartDocumentsConnectorProperties.username = "valtimo-test"
        smartDocumentsConnectorProperties.password = "41625e22-c4ef-487b-93fc-e46a25278d11"
        smartDocumentsConnectorProperties.templateGroup = "Development"
        connectorDeploymentService.deployAll(listOf(smartDocumentsConnector))

        val connectorType = connectorService.getConnectorTypes().first { it.name == "SmartDocuments" }
        val connectorInstanceId = ConnectorInstanceId.newId(UUID.fromString("731008bb-a062-4840-9d32-e29c08d32942"))
        val connectorInstance = ConnectorInstance(
            connectorInstanceId,
            connectorType,
            "test-connector",
            smartDocumentsConnectorProperties
        )
        connectorTypeInstanceRepository.save(connectorInstance)

        smartDocumentsConnector = connectorService.loadByClassName(smartDocumentsConnector::class.java)
    }
}