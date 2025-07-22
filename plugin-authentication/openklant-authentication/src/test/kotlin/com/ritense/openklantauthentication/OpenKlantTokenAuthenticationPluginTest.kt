/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.openklantauthentication

import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient
import kotlin.test.assertEquals

internal class OpenKlantTokenAuthenticationPluginTest {
    @Test
    fun `should add header to request`() {
        val plugin = OpenKlantTokenAuthenticationPlugin()
        plugin.token = "my-token"
        val restClientBuilder = RestClient.builder()

        plugin.applyAuth(restClientBuilder)

        val declaredField = restClientBuilder.javaClass.getDeclaredField("defaultHeaders")
        declaredField.trySetAccessible()
        val defaultHeaders = declaredField.get(restClientBuilder) as HttpHeaders
        assertEquals("Token my-token", defaultHeaders[HttpHeaders.AUTHORIZATION]?.first())
    }
}