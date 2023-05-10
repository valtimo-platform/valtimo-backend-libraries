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

package com.ritense.documentenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.security.DocumentenApiHttpSecurityConfigurer
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.documentenapi.web.rest.DocumentenApiResource
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.TemporaryResourceStorageService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class DocumentenApiAutoConfiguration {

    @Bean
    fun documentenApiClient(webclientBuilder: WebClient.Builder): DocumentenApiClient {
        return DocumentenApiClient(webclientBuilder)
    }

    @Bean
    fun documentenApiPluginFactory(
        pluginService: PluginService,
        client: DocumentenApiClient,
        storageService: TemporaryResourceStorageService,
        applicationEventPublisher: ApplicationEventPublisher,
        objectMapper: ObjectMapper,
    ): DocumentenApiPluginFactory {
        return DocumentenApiPluginFactory(
            pluginService,
            client,
            storageService,
            applicationEventPublisher,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenApiService::class)
    fun documentenApiService(
        pluginService: PluginService
    ): DocumentenApiService {
        return DocumentenApiService(pluginService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenApiResource::class)
    fun documentenApiResource(
        documentenApiService: DocumentenApiService
    ): DocumentenApiResource {
        return DocumentenApiResource(documentenApiService)
    }

    @Order(380)
    @Bean
    fun documentenApiHttpSecurityConfigurer(): DocumentenApiHttpSecurityConfigurer {
        return DocumentenApiHttpSecurityConfigurer()
    }

}
