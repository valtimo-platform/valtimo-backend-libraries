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

package com.ritense.objecttypenapi

import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.objecttypenapi.client.ObjecttypenApiClient
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginProperty
import java.net.URI

@Plugin(
   key = "objecttypenapi",
   title = "Objecttypen API",
   description = "Connects to the Objecttypen API"
)
class ObjecttypenApiPlugin(
    val objecttypenApiClient: ObjecttypenApiClient
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI
    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: ObjecttypenApiAuthentication

    fun getObjectType(typeUrl: URI): Objecttype {
        return objecttypenApiClient.getObjecttype(authenticationPluginConfiguration, typeUrl)
    }
}