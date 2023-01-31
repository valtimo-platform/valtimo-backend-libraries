/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.notificatiesapi.service

import com.ritense.notificatiesapi.domain.NotificatiesApiAbonnementLink
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.notificatiesapi.exception.AuthorizationException
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.zalando.problem.Status

class NotificatiesApiService(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val notificatiesApiAbonnementLinkRepository: NotificatiesApiAbonnementLinkRepository
) {

    fun handle(notification: NotificatiesApiNotificationReceivedEvent) {
        logger.debug { "Notification received: $notification" }
        applicationEventPublisher.publishEvent(
            notification
        )
    }

    fun findAbonnementSubscription(authHeader: String): NotificatiesApiAbonnementLink {
        return notificatiesApiAbonnementLinkRepository.findByAuth(authHeader)
            ?: throw AuthorizationException("", Status.UNAUTHORIZED)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}