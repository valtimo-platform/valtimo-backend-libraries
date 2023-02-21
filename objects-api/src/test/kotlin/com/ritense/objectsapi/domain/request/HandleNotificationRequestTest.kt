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

package com.ritense.objectsapi.domain.request

import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test

class HandleNotificationRequestTest {

    @Test
    fun `gets ObjectId from url`() {
        val handleNotificationRequest = HandleNotificationRequest(
            "test",
            "http://www.objects.api/api/v2/objects/5e973332-0e74-41b5-bdb9-083767ce388a",
            "test",
            emptyMap()
        )
        assertEquals(UUID.fromString("5e973332-0e74-41b5-bdb9-083767ce388a"), handleNotificationRequest.getObjectId())
    }

    @Test
    fun `is create notification when action type is create`() {
        val handleNotificationRequest = HandleNotificationRequest(
            "test",
            "http://www.objects.api/api/v2/objects/5e973332-0e74-41b5-bdb9-083767ce388a",
            "create",
            emptyMap()
        )
        assertTrue(handleNotificationRequest.isCreateNotification())
    }

    @Test
    fun `is not create notification when action type is not create`() {
        val handleNotificationRequest = HandleNotificationRequest(
            "test",
            "http://www.objects.api/api/v2/objects/5e973332-0e74-41b5-bdb9-083767ce388a",
            "test",
            emptyMap()
        )
        assertFalse(handleNotificationRequest.isCreateNotification())
    }

    @Test
    fun `is test notification when kanaal is test`() {
        val handleNotificationRequest = HandleNotificationRequest(
            "test",
            "http://www.objects.api/api/v2/objects/5e973332-0e74-41b5-bdb9-083767ce388a",
            "test",
            emptyMap()
        )
        assertTrue(handleNotificationRequest.isTestNotification())
    }

    @Test
    fun `is not test notification when kanaal is not test`() {
        val handleNotificationRequest = HandleNotificationRequest(
            "objecten",
            "http://www.objects.api/api/v2/objects/5e973332-0e74-41b5-bdb9-083767ce388a",
            "test",
            emptyMap()
        )
        assertFalse(handleNotificationRequest.isTestNotification())
    }
}