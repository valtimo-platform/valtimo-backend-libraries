/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.opennotificaties

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.domain.request.HandleNotificationRequest
import com.ritense.objectsapi.repository.AbonnementLinkRepository
import com.ritense.openzaak.service.ZaakService
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.service.OpenZaakService
import java.net.URI
import java.util.UUID
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher

@Deprecated("Since 12.0.0", ReplaceWith("com.ritense.notificatiesapi.service.NotificatiesApiService"))
class OpenNotificatieService(
    private val connectorService: ConnectorService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val abonnementLinkRepository: AbonnementLinkRepository,
    private val zaakService: ZaakService,
    private val openZaakService: OpenZaakService,
) {
    fun handle(notification: HandleNotificationRequest, connectorId: String, authorizationKey: String) {
        logger.debug { "Notification received: $notification" }

        if (notification.isTestNotification()) {
            return
        }

        applicationEventPublisher.publishEvent(
            OpenNotificationEvent(
                notification,
                connectorId,
                authorizationKey
            )
        )
    }

    fun findConnector(connectorId: String, authorizationKey: String): Connector {
        val connectorInstance = connectorService.getConnectorInstanceById(UUID.fromString(connectorId))
        if (!verifyAbonnementKey(ConnectorInstanceId(UUID.fromString(connectorId)), authorizationKey)) {
            throw InvalidKeyException()
        }
        return connectorService.load(connectorInstance)
    }

    private fun verifyAbonnementKey(connectorId: ConnectorInstanceId, key: String): Boolean {
        val abonnementLink = abonnementLinkRepository.findById(connectorId)
        return abonnementLink.isPresent && key == abonnementLink.get().key
    }

    fun createOpenzaakResources(files: List<URI>): Set<OpenZaakResource> {
        return files.map { openZaakService.store(zaakService.getInformatieObject(it)) }.toCollection(hashSetOf())
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}