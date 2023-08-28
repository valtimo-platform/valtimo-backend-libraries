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

package com.ritense.objectenapi

import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectenApiClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.net.URI
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.never

internal class ObjectenApiPluginTest{

    val client = mock<ObjectenApiClient>()
    val plugin = ObjectenApiPlugin(client)

    @BeforeEach
    fun setUp() {
        plugin.authenticationPluginConfiguration = mock()
        plugin.url = URI("http://example.com")
    }

    @Test
    fun `should call client on get object`() {
        val objectUrl = URI("http://example.com/1")
        val objectMock = mock<ObjectWrapper>()
        whenever(client.getObject(plugin.authenticationPluginConfiguration, objectUrl)).thenReturn(objectMock)

        val result = plugin.getObject(objectUrl)

        assertEquals(objectMock, result)
        verify(client).getObject(any(), any())
    }

    @Test
    fun `should call client on update object`() {
        val objectUrl =URI("http://example.com/1")
        val objectMock = mock<ObjectWrapper>()
        val objectRequest = mock<ObjectRequest>()
        whenever(client.objectUpdate(plugin.authenticationPluginConfiguration, objectUrl, objectRequest)).thenReturn(objectMock)

        val result = plugin.objectUpdate(objectUrl,  objectRequest)

        assertEquals(objectMock, result)
        verify(client).objectUpdate(any(), any(), any())
    }

    @Test
    fun `should call client on patch object`() {
        val objectUrl = URI("http://example.com/1")
        val objectMock = mock<ObjectWrapper>()
        val objectRequest = mock<ObjectRequest>()
        whenever(client.objectPatch(plugin.authenticationPluginConfiguration, objectUrl, objectRequest)).thenReturn(objectMock)

        val result = plugin.objectPatch(objectUrl,  objectRequest)

        assertEquals(objectMock, result)
        verify(client).objectPatch(any(), any(), any())
    }

    @Test
    fun `should call client on delete object`() {
        val objectUrl = URI("http://example.com/1")
        val mockStatus = mock<HttpStatus>()
        whenever(client.deleteObject(plugin.authenticationPluginConfiguration, objectUrl)).thenReturn(mockStatus)

        val result = plugin.deleteObject(objectUrl)

        assertEquals(mockStatus, result)
        verify(client).deleteObject(any(), any())
    }

    @Test
    fun `should fail on delete object due to url mismatch`() {
        val objectUrl = URI("http://localhost/1")

        assertThrows<IllegalStateException> {
            plugin.deleteObject(objectUrl)
        }

        verify(client, never()).deleteObject(any(), any())
    }
}