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

package com.ritense.connector.service

import com.ritense.connector.BaseIntegrationTest
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.domain.ConnectorTypeId
import com.ritense.connector.impl.DummyDependency
import com.ritense.connector.impl.NestedObject
import com.ritense.connector.impl.ObjectApiConnectorType
import com.ritense.connector.impl.ObjectApiProperties
import com.ritense.connector.repository.ConnectorTypeRepository
import com.ritense.connector.web.rest.request.ModifyConnectorInstanceRequest
import java.util.UUID
import javax.inject.Inject
import javax.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Pageable

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConnectorServiceIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var connectorService: ConnectorService

    @Inject
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    @Inject
    lateinit var connectorTypeRepository: ConnectorTypeRepository

    @Inject
    @Qualifier("test-connector")
    lateinit var connector: Connector

    lateinit var connectorTypeId: ConnectorTypeId

    @BeforeAll
    fun setup() {
        connectorTypeId = ConnectorTypeId(UUID.randomUUID())
        val connectorType = ConnectorType(
            connectorTypeId,
            "test-connector-name",
            "test-connector",
            object : ConnectorProperties {}
        )
        connectorTypeRepository.save(connectorType)
    }

    @Test
    fun `should create connectorInstance`() {
        val name = "aName"
        val objectApiProperties = ObjectApiProperties(NestedObject(name))

        val connectorInstance = connectorService.createConnectorInstance(
            connectorTypeId.id,
            name,
            objectApiProperties
        )

        assertThat(connectorInstance.id).isNotNull
        assertThat(connectorInstance.type.id).isEqualTo(connectorTypeId)
        assertThat(connectorInstance.name).isEqualTo(name)
        assertThat(connectorInstance.connectorProperties).isEqualTo(objectApiProperties)

        verify(connector, times(1)).onCreate(connectorInstance)
    }

    @Test
    fun `should modify connectorInstance`() {
        val connectorInstance = connectorService.createConnectorInstance(
            connectorTypeId.id,
            "aName",
            ObjectApiProperties(NestedObject("aCustomName"))
        )

        val request = ModifyConnectorInstanceRequest(
            connectorInstance.id.id,
            connectorInstance.type.id.id,
            "aNewName",
            ObjectApiProperties(NestedObject("aNewCustomPropertyValue"))
        )

        val result = connectorService.modifyConnectorTypeInstance(request)

        assertThat(result.connectorTypeInstance()).isNotNull
        assertThat(result.errors()).isEmpty()
        assertThat(result.connectorTypeInstance()?.name).isEqualTo(request.name)
        assertThat(result.connectorTypeInstance()?.connectorProperties).isEqualTo(request.connectorProperties)

        verify(connector, times(1)).onEdit(connectorInstance)
    }

    @Test
    fun `should remove connectorInstance`() {
        val connectorInstance = connectorService.createConnectorInstance(
            connectorTypeId.id,
            "aName",
            ObjectApiProperties(NestedObject("aCustomName"))
        )

        connectorService.removeConnectorTypeInstance(connectorInstance.id.id)

        verify(connector, times(1)).onDelete(connectorInstance)

        assertThrows(IllegalArgumentException::class.java) { connectorService.load("aName") }
    }

    @Test
    fun `should throw exception loading connector with unknown name`() {
        assertThrows(IllegalArgumentException::class.java) { connectorService.load("unknownName") }
    }

    @Test
    fun `should load connector with dependencies`() {
        val objectApiConnectorType = ObjectApiConnectorType(ObjectApiProperties(), DummyDependency())
        val connectorType = connectorDeploymentService.deployAll(listOf(objectApiConnectorType))[0]

        val nameIdentifier = "aCustomName"
        val properties = ObjectApiProperties(NestedObject("aCustomProperty")) //Typical set from frontend
        connectorService.createConnectorInstance(
            connectorType.id.id,
            nameIdentifier,
            properties
        )

        val connector = connectorService.load(nameIdentifier)
        assertThat(connector).isNotNull
        assertThat(connector.getProperties()).isEqualTo(properties)
        assertThat(connector.getProperties()).isInstanceOf(ObjectApiProperties::class.java)
    }

    @Test
    fun `should load connector with dependencies by class name`() {
        val objectApiConnectorType = ObjectApiConnectorType(ObjectApiProperties(), DummyDependency())
        val connectorType = connectorDeploymentService.deployAll(listOf(objectApiConnectorType))[0]
        val properties = ObjectApiProperties(NestedObject("aCustomProperty"))
        connectorService.createConnectorInstance(connectorType.id.id, "aCustomName", properties)

        val connector = connectorService.loadByClassName(ObjectApiConnectorType::class.java)

        assertThat(connector).isNotNull
        assertThat(connector.getProperties()).isEqualTo(properties)
        assertThat(connector.getProperties()).isInstanceOf(ObjectApiProperties::class.java)
    }

    @Test
    fun `should find connectorInstances`() {
        connectorService.createConnectorInstance(
            connectorTypeId.id,
            "aName",
            ObjectApiProperties(NestedObject("aCustomName"))
        )

        val connectorInstancesByType = connectorService.getConnectorInstancesByType(connectorTypeId.id, Pageable.unpaged())
        assertThat(connectorInstancesByType).isNotNull
        assertThat(connectorInstancesByType.totalElements).isEqualTo(1)
        assertThat(connectorInstancesByType.content.get(0).type.id).isEqualTo(connectorTypeId)
    }
}