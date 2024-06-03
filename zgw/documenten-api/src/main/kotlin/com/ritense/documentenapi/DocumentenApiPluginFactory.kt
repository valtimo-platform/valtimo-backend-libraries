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

package com.ritense.documentenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.service.DocumentDeleteHandler
import com.ritense.documentenapi.service.DocumentenApiVersionService
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.TemporaryResourceStorageService
import org.springframework.context.ApplicationEventPublisher

class DocumentenApiPluginFactory(
    pluginService: PluginService,
    private val client: DocumentenApiClient,
    private val storageService: TemporaryResourceStorageService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
    private val documentDeleteHandlers: List<DocumentDeleteHandler>,
    private val documentenApiVersionService: DocumentenApiVersionService,
) : PluginFactory<DocumentenApiPlugin>(pluginService) {

    override fun create(): DocumentenApiPlugin {
        return DocumentenApiPlugin(
            client,
            storageService,
            applicationEventPublisher,
            objectMapper,
            documentDeleteHandlers,
            documentenApiVersionService,
        )
    }
}