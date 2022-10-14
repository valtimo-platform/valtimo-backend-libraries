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

import com.ritense.connector.BaseTest
import com.ritense.connector.domain.Connector
import com.ritense.connector.impl.DummyDependency
import com.ritense.connector.impl.NestedObject
import com.ritense.connector.impl.ObjectApiConnectorType
import com.ritense.connector.impl.ObjectApiProperties
import com.ritense.connector.repository.ConnectorTypeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock

internal class ConnectorDeploymentServiceTest : BaseTest() {

    @Mock
    lateinit var connectorTypeRepository: ConnectorTypeRepository

    @BeforeEach
    fun setUp() {
        super.baseSetUp()
    }

    @Test
    fun `should deploy connectorBean and save to connectorType`() {
        val connectorDeploymentService = ConnectorDeploymentService(connectorTypeRepository)
        val objectApiProperties = ObjectApiProperties(NestedObject("aValue"))

        val connectorBeans = listOf<Connector>(
            ObjectApiConnectorType(
                objectApiProperties,
                DummyDependency()
            )
        )
        val connectorTypes = connectorDeploymentService.deployAll(connectorBeans)

        assertThat(connectorTypes).isNotNull
        assertThat(connectorTypes).hasSize(1)

        assertThat(connectorTypes[0].name).isEqualTo("test-connector")
        assertThat(connectorTypes[0].className).isEqualTo("objectApiConnectorType")
        assertThat(connectorTypes[0].connectorProperties).isEqualTo(objectApiProperties)
        assertThat(connectorTypes[0].allowMultipleConnectorInstances).isEqualTo(true)
    }
}
