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

package com.ritense.notificatiesapi.web.rest


import com.ritense.notificatiesapi.domain.NotificatiesApiAbonnementLink
import com.ritense.notificatiesapi.domain.NotificatiesApiConfigurationId
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.notificatiesapi.exception.AuthorizationException
import com.ritense.notificatiesapi.service.NotificatiesApiService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.util.UUID
import kotlin.test.assertEquals


class NotificatiesApiResourceTest {

    lateinit var notificatiesApiResource: NotificatiesApiResource
    lateinit var notificatiesApiService: NotificatiesApiService

    @BeforeEach
    fun beforeEach() {
        notificatiesApiService = mock()
        notificatiesApiResource = NotificatiesApiResource(
            notificatiesApiService
        )
    }

    @Test
    fun `send notification with success`() {
        doReturn(getAbonnementSubscrition()).whenever(notificatiesApiService).findAbonnementSubscription(any())
        doNothing().whenever(notificatiesApiService).handle(any())
        val result = notificatiesApiResource.handleNotification(createNotication(), "sdafads")
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
        verify(notificatiesApiService, times(1)).findAbonnementSubscription(any())
        verify(notificatiesApiService, times(1)).handle(any())
    }

    @Test
    fun `send notification without header`() {
        val result = notificatiesApiResource.handleNotification(createNotication(), null)
        assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
    }

    @Test
    fun `send notification with invalid auth`() {
        doThrow(AuthorizationException::class).whenever(notificatiesApiService).findAbonnementSubscription(any())
        val result = notificatiesApiResource.handleNotification(createNotication(), "anInvalidHeader")
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    private fun getAbonnementSubscrition(): NotificatiesApiAbonnementLink {
        return NotificatiesApiAbonnementLink(
            notificatiesApiConfigurationId = NotificatiesApiConfigurationId(UUID.randomUUID()),
            url = "",
            auth = ""
        )
    }

    private fun createNotication(): NotificatiesApiNotificationReceivedEvent {
        return NotificatiesApiNotificationReceivedEvent(
            kanaal = "",
            resourceUrl = "",
            actie = "",
            kenmerken = mapOf()
        )
    }

}