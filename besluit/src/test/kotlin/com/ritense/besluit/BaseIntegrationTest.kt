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

package com.ritense.besluit

import com.ritense.besluit.connector.BesluitConnector
import com.ritense.besluit.connector.BesluitProperties
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorService
import com.ritense.openzaak.catalogi.CatalogiClient
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.service.impl.Mapper
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
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
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@SpringBootTest
@ExtendWith(value = [SpringExtension::class, LiquibaseRunnerExtension::class])
@Tag("integration")
class BaseIntegrationTest : BaseTest() {

    @MockBean
    lateinit var catalogiClient: CatalogiClient

    @Autowired
    lateinit var besluitConnector: BesluitConnector

    @Autowired
    lateinit var besluitProperties: BesluitProperties

    @MockBean
    lateinit var userManagementService: UserManagementService

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    @Autowired
    lateinit var connectorTypeInstanceRepository: ConnectorTypeInstanceRepository

    @MockBean
    lateinit var mailSender: MailSender

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
                    "POST /api/v1/besluiten" -> mockResponseFromFile("/data/post-create-besluit.json")
                    "POST /zaken/api/v1/zaken" -> mockResponseFromFile("/data/post-create-zaak.json")
                    "GET /documenten/api/v1/enkelvoudiginformatieobjecten/429cd502-3ddc-43de-aa1b-791404cd2913" -> mockResponseFromFile("/data/get-enkelvoudiginformatieobject.json")
                    "POST /zaken/api/v1/zaakinformatieobjecten" -> mockResponseFromFile("/data/post-relation-zaak-informatieobject.json")
                    "POST /api/v1/besluitinformatieobjecten" -> mockResponseFromFile("/data/post-relation-besluit-informatieobject.json")
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
        besluitProperties.url = server.url("/").toString()
        besluitProperties.clientId = "valtimo-test"
        besluitProperties.secret = "41625e21-c4ef-487b-93fc-e46a25278d12"
        besluitProperties.rsin = Rsin("051845623")
        connectorDeploymentService.deployAll(listOf(besluitConnector))

        val connectorType = connectorService.getConnectorTypes().first { it.name == "Besluiten" }
        val connectorInstanceId = ConnectorInstanceId.newId(UUID.fromString("731008ba-a062-4840-9d32-e29c08d32943"))
        val connectorInstance = ConnectorInstance(
            connectorInstanceId,
            connectorType,
            "test-connector",
            besluitProperties
        )
        connectorTypeInstanceRepository.save(connectorInstance)

        besluitConnector = connectorService.loadByClassName(BesluitConnector::class.java)
    }

    private fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(readFileAsString(fileName))
    }

    fun findRequest(method: HttpMethod, path: String): RecordedRequest? {
        return executedRequests
            .filter { method.matches(it.method!!) }
            .firstOrNull { it.path?.substringBefore('?').equals(path) }
    }

    fun <T> getRequestBody(method: HttpMethod, path: String, clazz: Class<T>): T {
        return Mapper.get().readValue(findRequest(method, path)!!.body.readUtf8(), clazz)
    }
}

