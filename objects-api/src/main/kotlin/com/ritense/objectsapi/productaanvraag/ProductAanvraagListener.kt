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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector.Companion.OBJECTEN_KANAAL_NAME
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.opennotificaties.OpenNotificationEvent
import org.springframework.context.event.EventListener

class ProductAanvraagListener(
    private val productAanvraagService: ProductAanvraagService,
    private val openNotificatieService: OpenNotificatieService,
) {

    @EventListener(OpenNotificationEvent::class)
    fun notificationReceived(event: OpenNotificationEvent) {
        if (event.notification.kanaal == OBJECTEN_KANAAL_NAME
            && event.notification.isCreateNotification()
        ) {

            val connector = openNotificatieService.findConnector(event.connectorId, event.authorizationKey) as ProductAanvraagConnector
            val productAanvraagId = event.notification.getObjectId()
            val productAanvraag = connector.getProductAanvraag(productAanvraagId)
            val typeMapping = connector.getTypeMapping(productAanvraag.type)
            val aanvragerRolTypeUrl = connector.getAanvragerRolTypeUrl()

            //TODO: TP32743 Redflag needs to be refactored ASAP
            (productAanvraag.data as ObjectNode).set<TextNode>("\$bsn", TextNode(productAanvraag.bsn))

            productAanvraagService.createDossier(productAanvraag, typeMapping, aanvragerRolTypeUrl)
            connector.deleteProductAanvraag(productAanvraagId)
        }
    }
}