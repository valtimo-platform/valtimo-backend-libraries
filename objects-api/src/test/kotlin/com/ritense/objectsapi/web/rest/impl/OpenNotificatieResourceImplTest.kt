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

import com.ritense.objectsapi.BaseTest
import com.ritense.objectsapi.domain.request.HandleNotificationRequest
import com.ritense.objectsapi.opennotificaties.InvalidKeyException
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus

class OpenNotificatieResourceImplTest : BaseTest() {
    lateinit var openNotificatieService: OpenNotificatieService
    lateinit var openNotificatieResourceImpl: OpenNotificatieResourceImpl
    val handleNotificationRequest = HandleNotificationRequest(
        "kanaal",
        "resourceUrl",
        "actie",
        emptyMap()
    )

    @BeforeEach
    fun setup() {
        openNotificatieService = mock(OpenNotificatieService::class.java)
        openNotificatieResourceImpl = OpenNotificatieResourceImpl(openNotificatieService)
    }

    @Test
    fun `handle notification should handle notification and return ok`() {
        val responseEntity =
            openNotificatieResourceImpl.handleNotification(handleNotificationRequest, "test", "connectorId")

        verify(openNotificatieService, times(1)).handle(handleNotificationRequest, "connectorId", "test")
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.statusCode)
    }

    @Test
    fun `handle notification when no authorization header is present should return unauthorized`() {
        val responseEntity =
            openNotificatieResourceImpl.handleNotification(handleNotificationRequest, null, "connectorId")

        verify(openNotificatieService, never()).handle(any(), anyString(), anyString())
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.statusCode)
    }

    @Test
    fun `handle notification should return forbidden on InvalidKeyException`() {
        `when`(openNotificatieService.handle(handleNotificationRequest, "connectorId", "test"))
            .thenThrow(InvalidKeyException())
        val responseEntity =
            openNotificatieResourceImpl.handleNotification(handleNotificationRequest, "test", "connectorId")

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.statusCode)
    }
}