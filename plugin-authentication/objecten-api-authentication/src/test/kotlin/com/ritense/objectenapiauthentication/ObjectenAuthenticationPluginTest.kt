/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.objectenapiauthentication

import java.net.URI
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

internal class ObjectenAuthenticationPluginTest {
    @Test
    fun `should add header to request`() {
        val requestCaptor = argumentCaptor<ClientRequest>()
        val plugin = ObjectTokenAuthenticationPlugin()
        plugin.token = "token"

        val request = ClientRequest.create(
            HttpMethod.GET,
            URI.create("http://some-url.tld")).build()
        val nextFilter = mock<ExchangeFunction>()
        val response = mock<Mono<ClientResponse>>()
        whenever(nextFilter.exchange(any())).thenReturn(response)

        plugin.filter(request, nextFilter)

        verify(nextFilter).exchange(requestCaptor.capture())
        assertEquals("Token token", requestCaptor.firstValue.headers()["Authorization"]?.get(0))
    }
}