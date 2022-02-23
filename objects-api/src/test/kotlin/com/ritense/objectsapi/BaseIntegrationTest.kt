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

package com.ritense.objectsapi

import com.ritense.connector.domain.Connector
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorService
import com.ritense.klant.service.BurgerService
import com.ritense.objectsapi.opennotificaties.OpenNotificatieProperties
import com.ritense.objectsapi.service.ObjectTypeConfig
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.objectsapi.service.ServerAuthSpecification
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.mail.MailSender
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@SpringBootTest
@ExtendWith(value = [SpringExtension::class, LiquibaseRunnerExtension::class])
@Tag("integration")
abstract class BaseIntegrationTest {

    @MockBean
    lateinit var userManagementService: UserManagementService

    @MockBean
    lateinit var burgerService: BurgerService

    @MockBean
    lateinit var mailSender: MailSender

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    @Autowired
    @Qualifier("objectsApiConnector")
    lateinit var objectsApiConnector: Connector

    @Autowired
    @Qualifier("openNotificatieConnector")
    lateinit var openNotificatieConnector: Connector

    fun setupObjectApiConnector(url: String) {
        val properties = ObjectsApiProperties(
            ServerAuthSpecification(url, UUID.randomUUID().toString()),
            ServerAuthSpecification(url, UUID.randomUUID().toString()),
            ObjectTypeConfig("some-name", "Objecttypen API: productaanvraag", url, "1")
        )

        connectorDeploymentService.deployAll(listOf(objectsApiConnector))
        val connectorType = connectorService.getConnectorTypes().first { it.name == "ObjectsApi" }

        connectorService.createConnectorInstance(
            connectorType.id.id,
            "objectsApiInstance",
            properties
        )

        objectsApiConnector = connectorService.loadByClassName(objectsApiConnector::class.java)
    }

    fun setupOpenNotificatieConnector(url: String) {
        val properties = OpenNotificatieProperties(
            url,
            "some-client-id",
            UUID.randomUUID().toString(),
            url
        )

        connectorDeploymentService.deployAll(listOf(openNotificatieConnector))
        val connectorType = connectorService.getConnectorTypes().first { it.name == "OpenNotificatie" }

        connectorService.createConnectorInstance(
            connectorType.id.id,
            "openNotificatieInstance",
            properties
        )

        openNotificatieConnector = connectorService.loadByClassName(openNotificatieConnector::class.java)
    }

    companion object {
        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
        }
    }
}