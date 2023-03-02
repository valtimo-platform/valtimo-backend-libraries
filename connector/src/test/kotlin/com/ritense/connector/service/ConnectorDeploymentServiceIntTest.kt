/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import javax.inject.Inject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Transactional
internal class ConnectorDeploymentServiceIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    @Autowired
    @Qualifier("objectApiConnectorType")
    lateinit var connector: Connector

    @Test
    fun `should deploy connectorBean and save to connectorType`() {
        val connectorTypes = connectorDeploymentService.deployAll(listOf(connector))

        assertThat(connectorTypes).isNotNull
        assertThat(connectorTypes).hasSize(1)

        assertThat(connectorTypes[0].name).isEqualTo("aTypeName")
        assertThat(connectorTypes[0].className).isEqualTo("objectApiConnectorType")
        assertThat(connectorTypes[0].connectorProperties).isInstanceOf(ConnectorProperties::class.java)
    }
}