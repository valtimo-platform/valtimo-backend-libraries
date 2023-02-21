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

package com.ritense.connector.autodeployment

import com.ritense.connector.domain.Connector
import com.ritense.connector.service.ConnectorDeploymentService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

class ConnectorApplicationReadyEventListener(
    private val connectorDeploymentService: ConnectorDeploymentService,
    private val connectors: List<Connector>
) {

    @Order(1)
    @EventListener(ApplicationReadyEvent::class)
    fun handle() {
        connectorDeploymentService.deployAll(connectors)
    }
}