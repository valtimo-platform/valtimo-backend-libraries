/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.contactmoment.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorFluentBuilder
import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.BaseIntegrationTest
import com.ritense.contactmoment.domain.request.CreateContactMomentRequest
import java.util.UUID
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient

@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContactMomentConnectorIntTest : BaseIntegrationTest() {

    @Autowired
    @Qualifier("contactMomentConnector")
    lateinit var contactMomentConnector: Connector

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var connectorTypeInstanceRepository: ConnectorTypeInstanceRepository

    @Autowired
    lateinit var contactMomentProperties: ContactMomentProperties

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    @Autowired
    lateinit var connectorFluentBuilder: ConnectorFluentBuilder

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

    @Test
    fun `should create contactmoment`() {
        val contactMoment = (contactMomentConnector as ContactMomentConnector).createContactMoment("Hello, ...", "mail")

        Assertions.assertThat(contactMoment.url)
            .isEqualTo("http://localhost:8006/contactmomenten/api/v1/contactmomenten/4e517f3f-1f2c-41cb-bf11-75c86c7ca51b")
    }

    fun startMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path?.substringBefore('?')) {
                    "/contactmomenten/api/v1/contactmomenten" -> mockResponseFromFile("/data/post-contact-moment.json")
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server = MockWebServer()
        server.dispatcher = dispatcher
        server.start()
    }

    private fun setupConnector() {
        contactMomentProperties.url = server.url("/").toString()
        contactMomentProperties.clientId = "valtimo-test"
        contactMomentProperties.secret = " 41625e21-c4ef-487b-93fc-e46a25278d11"
        connectorDeploymentService.deployAll(listOf(contactMomentConnector))

        val connectorType = connectorService.getConnectorTypes().first { it.name == "ContactMoment" }
        val connectorInstanceId = ConnectorInstanceId.newId(UUID.fromString("731008ba-a062-4840-9d32-e29c08d32942"))
        val connectorInstance = ConnectorInstance(
            connectorInstanceId,
            connectorType,
            "test-connector",
            contactMomentProperties
        )
        connectorTypeInstanceRepository.save(connectorInstance)

        contactMomentConnector = connectorFluentBuilder
            .builder()
            .withConnector(connectorInstance.name) as ContactMomentConnector
    }
}