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

package com.ritense.catalogiapi

import com.ritense.catalogiapi.client.CatalogiApiClient
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.DocumentService
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService

class CatalogiApiPluginFactory(
    pluginService: PluginService,
    val client: CatalogiApiClient,
    val zaaktypeUrlProvider: ZaaktypeUrlProvider,
    val documentService: DocumentService<JsonSchemaDocument>,
) : PluginFactory<CatalogiApiPlugin>(pluginService) {

    override fun create(): CatalogiApiPlugin {
        return CatalogiApiPlugin(client, zaaktypeUrlProvider, documentService)
    }
}
