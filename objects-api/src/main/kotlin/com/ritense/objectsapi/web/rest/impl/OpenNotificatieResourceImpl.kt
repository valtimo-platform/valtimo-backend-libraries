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

package com.ritense.objectsapi.web.rest.impl

import com.ritense.objectsapi.domain.request.HandleNotificationRequest
import com.ritense.objectsapi.opennotificaties.InvalidKeyException
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.web.rest.OpenNotificatieResource
import org.springframework.http.ResponseEntity

class OpenNotificatieResourceImpl(
    val openNotificatieService: OpenNotificatieService
): OpenNotificatieResource {

    // There's no regular JWT protection on this endpoint. Instead, it is based on an API key generated at creation of
    // the Open Notificatie abonnement
    override fun handleNotification(
        notification: HandleNotificationRequest,
        authHeader: String?,
        connectorId: String
    ): ResponseEntity<Void> {
        if (authHeader != null) {
            try {
                openNotificatieService.handle(notification, connectorId, authHeader)
                return ResponseEntity.noContent().build()
            } catch (ike: InvalidKeyException) {
                return ResponseEntity.status(403).build()
            }
        } else {
            return ResponseEntity.status(401).build()
        }
    }
}
