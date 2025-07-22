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

import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginProperty
import org.hibernate.validator.constraints.Length
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient

@Plugin(
    key = "openklanttokenauthentication",
    title = "OpenKlant Token Authentication",
    description = "Plugin used to provide authentication based on a token"
)
class OpenKlantTokenAuthenticationPlugin {

    @Length(min = 20)
    @PluginProperty(key = "token", secret = true, required = true)
    lateinit var token: String

    fun applyAuth(builder: RestClient.Builder): RestClient.Builder {
        return builder.defaultHeaders { headers ->
            headers[HttpHeaders.AUTHORIZATION] = "Token $token"
        }
    }
}