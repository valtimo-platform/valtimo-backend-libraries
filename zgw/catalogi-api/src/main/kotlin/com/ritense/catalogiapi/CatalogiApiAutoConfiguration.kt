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
import com.ritense.catalogiapi.security.CatalogiApiHttpSecurityConfigurer
import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.catalogiapi.web.rest.CatalogiResource
import com.ritense.document.service.DocumentService
import com.ritense.plugin.service.PluginService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class CatalogiApiAutoConfiguration {

    @Bean
    fun catalogiApiClient(webclientBuilder: WebClient.Builder): CatalogiApiClient {
        return CatalogiApiClient(webclientBuilder)
    }

    @Bean
    fun catalogiApiPluginFactory(
        pluginService: PluginService,
        client: CatalogiApiClient,
        zaaktypeUrlProvider: ZaaktypeUrlProvider,
        documentService: DocumentService,
    ): CatalogiApiPluginFactory {
        return CatalogiApiPluginFactory(pluginService, client, zaaktypeUrlProvider, documentService)
    }

    @Bean
    @ConditionalOnMissingBean(CatalogiService::class)
    fun catalogiService(
        zaaktypeUrlProvider: ZaaktypeUrlProvider,
        pluginService : PluginService
    ): CatalogiService {
        return CatalogiService(zaaktypeUrlProvider, pluginService)
    }

    @Bean
    @ConditionalOnMissingBean(CatalogiResource::class)
    fun catalogiResource(
        catalogiService: CatalogiService
    ): CatalogiResource {
        return CatalogiResource(catalogiService)
    }

    @Order(400)
    @Bean
    fun catalogiApiHttpSecurityConfigurer(): CatalogiApiHttpSecurityConfigurer {
        return CatalogiApiHttpSecurityConfigurer()
    }
}
