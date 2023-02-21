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

package com.ritense.smartdocuments.autoconfigure

import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.plugin.SmartDocumentsPlugin
import com.ritense.smartdocuments.plugin.SmartDocumentsPluginFactory
import com.ritense.valueresolver.ValueResolverService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(PluginFactory::class)
class SmartDocumentsPluginAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SmartDocumentsPluginFactory::class)
    fun smartDocumentsPluginFactory(
        processDocumentService: ProcessDocumentService,
        applicationEventPublisher: ApplicationEventPublisher,
        smartDocumentsClient: SmartDocumentsClient,
        valueResolverService: ValueResolverService,
        temporaryResourceStorageService: TemporaryResourceStorageService,
        pluginService: PluginService
    ): PluginFactory<SmartDocumentsPlugin> {
        return SmartDocumentsPluginFactory(
            processDocumentService,
            applicationEventPublisher,
            smartDocumentsClient,
            valueResolverService,
            temporaryResourceStorageService,
            pluginService
        )
    }
}
