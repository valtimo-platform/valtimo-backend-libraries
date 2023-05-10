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

package com.ritense.objectenapiauthentication

import com.ritense.objectenapi.ObjectenApiAuthentication
import com.ritense.objecttypenapi.ObjecttypenApiAuthentication
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginProperty
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Plugin(
    key = "objecttokenauthentication",
    title = "Object Token Authentication",
    description = "Plugin used to provide authentication based on a token"
)
class ObjectTokenAuthenticationPlugin
    : ObjectenApiAuthentication, ObjecttypenApiAuthentication {

    @PluginProperty(key = "token", secret = true, required = true)
    lateinit var token: String

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val filteredRequest = ClientRequest.from(request).headers {
            headers -> headers.set(HttpHeaders.AUTHORIZATION, "Token $token")
        }.build()
        return next.exchange(filteredRequest)
    }
}