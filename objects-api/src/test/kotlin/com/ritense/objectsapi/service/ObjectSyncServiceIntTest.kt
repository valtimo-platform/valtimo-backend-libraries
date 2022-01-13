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

package com.ritense.objectsapi.service

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.BaseIntegrationTest
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.request.ModifyObjectSyncConfigRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.util.UUID
import javax.transaction.Transactional

@Transactional
class ObjectSyncServiceIntTest : BaseIntegrationTest() {

    @Autowired
    @Qualifier("objectsApiConnector")
    lateinit var objectsApiConnector: Connector

    @Autowired
    lateinit var objectSyncService: ObjectSyncService

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    lateinit var connectorInstance: ConnectorInstance

    @BeforeEach
    internal fun setUp() {
        val connectorTypes = connectorDeploymentService.deployAll(listOf(objectsApiConnector))
        connectorInstance = connectorService.createConnectorInstance(
            typeId = connectorTypes.get(0).id.id,
            name = "testInstance",
            connectorProperties = ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(),
                objectsTypeApi = ServerAuthSpecification(),
                objectType = ObjectTypeConfig()
            )
        ).connectorTypeInstance()!!
    }

    @Test
    fun `should create objectSyncConfig`() {

        val request = CreateObjectSyncConfigRequest(
            connectorInstanceId = connectorInstance.id.id,
            enabled = true,
            documentDefinitionName = "someDefinition",
            objectTypeId = UUID.randomUUID()
        )

        val result = objectSyncService.createObjectSyncConfig(request)

        assertThat(result.objectSyncConfig()).isNotNull
        assertThat(result.objectSyncConfig()?.connectorInstanceId).isEqualTo(request.connectorInstanceId)
        assertThat(result.objectSyncConfig()?.enabled).isEqualTo(request.enabled)
        assertThat(result.objectSyncConfig()?.documentDefinitionName).isEqualTo(request.documentDefinitionName)
        assertThat(result.objectSyncConfig()?.objectTypeId).isEqualTo(request.objectTypeId)
    }

    @Test
    fun `should modify objectSyncConfig`() {
        val request1 = CreateObjectSyncConfigRequest(
            connectorInstanceId = connectorInstance.id.id,
            enabled = true,
            documentDefinitionName = "someDefinition",
            objectTypeId = UUID.randomUUID()
        )

        val result = objectSyncService.createObjectSyncConfig(request1)

        val request2 = ModifyObjectSyncConfigRequest(
            id = result.objectSyncConfig()?.id!!.id,
            connectorInstanceId = connectorInstance.id.id,
            enabled = false,
            documentDefinitionName = "anotherDefinition",
            objectTypeId = UUID.randomUUID()
        )

        val result2 = objectSyncService.modifyObjectSyncConfig(request2)

        assertThat(result2.objectSyncConfig()).isNotNull
        assertThat(result2.errors()).isEmpty()
        assertThat(result2.objectSyncConfig()?.enabled).isEqualTo(request2.enabled)
        assertThat(result2.objectSyncConfig()?.documentDefinitionName).isEqualTo(request2.documentDefinitionName)
        assertThat(result2.objectSyncConfig()?.objectTypeId).isEqualTo(request2.objectTypeId)
    }

    @Test
    fun `should remove connectorInstance`() {

        val request = CreateObjectSyncConfigRequest(
            connectorInstanceId = connectorInstance.id.id,
            enabled = true,
            documentDefinitionName = "someDefinition",
            objectTypeId = UUID.randomUUID()
        )

        val result = objectSyncService.createObjectSyncConfig(request)

        objectSyncService.removeObjectSyncConfig(result.objectSyncConfig()?.id!!.id)

        val page = objectSyncService.getObjectSyncConfig("someDefinition")
        assertThat(page.totalElements).isEqualTo(0)
    }

    @Test
    fun `should find objectSyncConfig`() {
        val request = CreateObjectSyncConfigRequest(
            connectorInstanceId = connectorInstance.id.id,
            enabled = true,
            documentDefinitionName = "someDefinition",
            objectTypeId = UUID.randomUUID()
        )

        objectSyncService.createObjectSyncConfig(request)

        val page = objectSyncService.getObjectSyncConfig("someDefinition")

        assertThat(page).isNotNull
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content[0].documentDefinitionName).isEqualTo("someDefinition")
    }
}