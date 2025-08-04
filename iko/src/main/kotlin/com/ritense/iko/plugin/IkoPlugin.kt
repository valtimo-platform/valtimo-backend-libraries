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

package com.ritense.iko.plugin

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.iko.client.IkoClient
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.valtimo.contract.validation.Url
import java.net.URI

@Plugin(
    key = "iko",
    title = "IKO",
    description = "Connects to the IKO Server"
)
class IkoPlugin(
    private val ikoClient: IkoClient,
) {
    @Url
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    fun getById(
        endpointPath: String,
        id: String,
    ): JsonNode {
        return ikoClient.getById(
            baseUrl = url,
            endpointPath = endpointPath,
            id = id,
        )
    }

    fun search(
        endpointPath: String,
        endpointType: String? = null,
        filters: Map<String, String>,
    ): JsonNode {
        return ikoClient.search(
            baseUrl = url,
            endpointPath = endpointPath,
            endpointType = endpointType,
            filters = filters,
        )
    }
}
