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

package com.ritense.objectsapi.productaanvraag

import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector.Companion.OBJECTEN_KANAAL_NAME
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.opennotificaties.OpenNotificationEvent
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import javax.persistence.EntityNotFoundException

class ProductAanvraagListener(
    private val productAanvraagService: ProductAanvraagService,
    private val openNotificatieService: OpenNotificatieService,
) {

    @EventListener(OpenNotificationEvent::class)
    fun notificationReceived(event: OpenNotificationEvent) {
        if (event.notification.kanaal == OBJECTEN_KANAAL_NAME
            && event.notification.isCreateNotification()
        ) {
            try {
                val connector = try {
                    openNotificatieService.findConnector(event.connectorId, event.authorizationKey)
                } catch (e: EntityNotFoundException) {
                    logger.error { "Failed to find connector with id '${event.connectorId}'" }
                    return
                }

                if (connector is ProductAanvraagConnector && connector.getObjectsApiConnector().getProperties()
                        .objectType.url == event.notification.getObjectTypeUrl()
                ) {
                    val productAanvraagId = event.notification.getObjectId()
                    val productAanvraag = connector.getProductAanvraag(productAanvraagId)
                    val typeMapping = connector.getTypeMapping(productAanvraag.type)
                    val aanvragerRolTypeUrl = connector.getAanvragerRolTypeUrl()

                    productAanvraagService.createDossier(productAanvraag, typeMapping, aanvragerRolTypeUrl)
                    connector.deleteProductAanvraag(productAanvraagId)
                }
            } catch (e: Exception) {
                logger.error { "Failed handle ProductAanvraag. Connector id:  '${event.connectorId}'. Error: ${e.message}" }
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
