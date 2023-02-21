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

package com.ritense.objecttypenapi

import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.objecttypenapi.client.ObjecttypenApiClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI

internal class ObjecttypenApiPluginTest{

    val client = mock<ObjecttypenApiClient>()
    val plugin = ObjecttypenApiPlugin(client)

    @BeforeEach
    fun setUp() {
        plugin.authenticationPluginConfiguration = mock()
        plugin.url = mock()
    }

    @Test
    fun `should call client on get object`() {
        val objecttypeUrl = URI("http://example.com")
        val objecttypeMock = mock<Objecttype>()
        whenever(client.getObjecttype(plugin.authenticationPluginConfiguration, objecttypeUrl)).thenReturn(objecttypeMock)

        val result = plugin.getObjecttype(objecttypeUrl)

        assertEquals(objecttypeMock, result)
        verify(client).getObjecttype(any(), any())
    }
}