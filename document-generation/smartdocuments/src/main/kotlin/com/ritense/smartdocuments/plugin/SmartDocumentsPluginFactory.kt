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
package com.ritense.smartdocuments.plugin

import com.ritense.document.service.DocumentService
import com.ritense.plugin.factory.PluginFactory
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.ResourceService
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.valtimo.contract.json.Mapper
import org.springframework.context.ApplicationEventPublisher

class SmartDocumentsPluginFactory(
    private val pluginService: PluginService,
    private val documentService: DocumentService,
    private val resourceService: ResourceService,
    private val processDocumentService: ProcessDocumentService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val smartDocumentsClient: SmartDocumentsClient,
) : PluginFactory<SmartDocumentsPlugin> {

    override fun create(pluginConfigurationKey: String): SmartDocumentsPlugin {
        val configuration = pluginService.getPluginConfiguration(pluginConfigurationKey)
        return SmartDocumentsPlugin(
            documentService,
            resourceService,
            processDocumentService,
            applicationEventPublisher,
            smartDocumentsClient,
            Mapper.INSTANCE.get().readValue(configuration.properties, SmartDocumentsPluginProperties::class.java)
        )
    }
}
