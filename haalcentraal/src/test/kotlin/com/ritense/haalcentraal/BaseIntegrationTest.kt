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

package com.ritense.haalcentraal

import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorService
import com.ritense.haalcentraal.connector.HaalCentraalBRPConnector
import com.ritense.haalcentraal.connector.HaalCentraalBRPProperties
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(value = [SpringExtension::class, LiquibaseRunnerExtension::class])
@Tag("integration")
class BaseIntegrationTest : BaseTest() {

    @Autowired
    lateinit var haalCentraalBRPConnector: HaalCentraalBRPConnector

    @Autowired
    lateinit var haalCentraalBRPProperties: HaalCentraalBRPProperties

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    @MockBean
    lateinit var userManagementService: UserManagementService

    lateinit var server: MockWebServer
    protected var executedRequests: MutableList<RecordedRequest> = mutableListOf()

    @BeforeEach
    internal fun setUp() {
        startMockServer()
        setupConnector()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    fun startMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val response = when (request.method + " " + request.path?.substringBefore('?')) {
                    "POST /api/v1/haalCentraalBRPen" -> mockResponseFromFile("/data/get-ingeschreven-personen.json")
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
        haalCentraalBRPProperties.url = server.url("/").toString()
        haalCentraalBRPProperties.apiKey = "8b5c1ce2-0942-4046-b20b-43a01b897d5a"
        connectorDeploymentService.deployAll(listOf(haalCentraalBRPConnector))

        val connectorType = connectorService.getConnectorTypes().first { it.name == "HaalCentraal" }

        connectorService.createConnectorInstance(
            connectorType.id.id,
            "haalcentraalConnector",
            haalCentraalBRPProperties
        )
        haalCentraalBRPConnector = connectorService.loadByClassName(HaalCentraalBRPConnector::class.java)
    }

}

