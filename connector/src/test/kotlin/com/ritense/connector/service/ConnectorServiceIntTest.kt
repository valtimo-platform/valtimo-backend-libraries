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
import com.ritense.connector.web.rest.result.CreateConnectorInstanceResultFailed
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Pageable
import java.util.UUID
import javax.inject.Inject
import javax.transaction.Transactional

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
    lateinit var connectorType: ConnectorType

    @BeforeAll
    fun setup() {
        connectorTypeId = ConnectorTypeId(UUID.randomUUID())
        connectorType = ConnectorType(
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

        val connectorInstanceResult = connectorService.createConnectorInstance(
            connectorTypeId.id,
            name,
            objectApiProperties
        )

        assertThat(connectorInstanceResult.connectorTypeInstance()).isNotNull

        val connectorInstance = connectorInstanceResult.connectorTypeInstance()!!
        assertThat(connectorInstance.id).isNotNull
        assertThat(connectorInstance.type.id).isEqualTo(connectorTypeId)
        assertThat(connectorInstance.name).isEqualTo(name)
        assertThat(connectorInstance.connectorProperties).isEqualTo(objectApiProperties)

        verify(connector, times(1)).onCreate(connectorInstance)
    }

    @Test
    fun `should modify connectorInstance`() {
        val connectorInstanceResult = connectorService.createConnectorInstance(
            connectorTypeId.id,
            "aName",
            ObjectApiProperties(NestedObject("aCustomName"))
        )

        assertThat(connectorInstanceResult.connectorTypeInstance()).isNotNull
        val connectorInstance = connectorInstanceResult.connectorTypeInstance()!!

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
        val connectorInstanceResult = connectorService.createConnectorInstance(
            connectorTypeId.id,
            "aName",
            ObjectApiProperties(NestedObject("aCustomName"))
        )

        assertThat(connectorInstanceResult.connectorTypeInstance()).isNotNull
        val connectorInstance = connectorInstanceResult.connectorTypeInstance()!!

        connectorService.removeConnectorTypeInstance(connectorInstance.id.id)

        verify(connector, times(1)).onDelete(connectorInstance)

        assertThrows(IllegalArgumentException::class.java) { connectorService.load("aName") }
    }

    @Test
    fun `should throw exception loading connector with unknown name`() {
        assertThrows(IllegalArgumentException::class.java) { connectorService.load("unknownName") }
    }

    @Test
    fun `should fail when using the same name twice`() {
        val name = "aName"
        val objectApiProperties = ObjectApiProperties(NestedObject(name))

        connectorService.createConnectorInstance(
            connectorTypeId.id,
            name,
            objectApiProperties
        )

        val result = connectorService.createConnectorInstance(
            connectorTypeId.id,
            name,
            objectApiProperties
        )

        assertThat(result).isInstanceOf(CreateConnectorInstanceResultFailed::class.java)
        assertThat(result.connectorTypeInstance()).isNull()
    }

    @Test
    fun `should allow multiple connectors of same type when multiple is allowed`() {
        assertThat(connectorType.allowMultipleConnectorInstances).isTrue

        val name = "aName"
        val objectApiProperties = ObjectApiProperties(NestedObject(name))
        connectorService.createConnectorInstance(
            connectorTypeId.id,
            name,
            objectApiProperties
        )

        val result = connectorService.createConnectorInstance(
            connectorTypeId.id,
            "AnotherName",
            objectApiProperties
        )

        assertThat(result).isNotNull
    }

    @Test
    fun `should fail when creating connector of a type that already exists and where multiple are not allowed`() {
        val name = "singleInstanceConnector"
        val objectApiProperties = ObjectApiProperties(NestedObject(name))
        val singleInstanceConnectorId = ConnectorTypeId(UUID.randomUUID())

        val singleInstanceConnectorType = ConnectorType(
            singleInstanceConnectorId,
            "test-connector-name",
            "test-connector",
            object : ConnectorProperties {},
            false
        )
        connectorTypeRepository.save(singleInstanceConnectorType)

        // First connector instance
        connectorService.createConnectorInstance(
            singleInstanceConnectorId.id,
            name,
            objectApiProperties
        )

        // Second connector instance
        val result =  connectorService.createConnectorInstance(
            singleInstanceConnectorId.id,
            "otherName",
            objectApiProperties
        )

        assertThat(result).isInstanceOf(CreateConnectorInstanceResultFailed::class.java)
        assertThat(result.connectorTypeInstance()).isNull()
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