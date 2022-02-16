/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.domain.ResourceId
import com.ritense.resource.repository.OpenZaakResourceRepository
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import java.net.URI
import java.util.UUID

class OpenNotificatieService(
    private val connectorService: ConnectorService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val abonnementLinkRepository: AbonnementLinkRepository,
    private val zaakService: ZaakService,
    private val openZaakResourceRepository: OpenZaakResourceRepository,
) {
    fun handle(notification: HandleNotificationRequest, connectorId: String, authorizationKey: String) {
        if (notification.isTestNotification()) {
            logger.info { "Test notification received" }
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
        return files.map { createOpenzaakResource(getInformatieObject(it)) }.toCollection(hashSetOf())
    }

    private fun getInformatieObject(file: URI): InformatieObject {
        return zaakService.getInformatieObject(UUID.fromString(file.path.substringAfterLast('/')))
    }

    private fun createOpenzaakResource(informatieObject: InformatieObject): OpenZaakResource {
        val openZaakResource = OpenZaakResource(
            ResourceId.newId(UUID.randomUUID()),
            informatieObject.url,
            informatieObject.bestandsnaam,
            informatieObject.bestandsnaam.substringAfterLast("."),
            informatieObject.bestandsomvang,
            informatieObject.beginRegistratie
        )
        return openZaakResourceRepository.save(openZaakResource)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}