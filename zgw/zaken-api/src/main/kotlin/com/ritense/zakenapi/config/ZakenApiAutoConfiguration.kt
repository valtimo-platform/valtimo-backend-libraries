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

package com.ritense.zakenapi.config

import com.ritense.document.service.DocumentService
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.ResourceProvider
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPluginFactory
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zakenapi.service.ZaakDocumentService
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.zakenapi.repository"])
@EntityScan("com.ritense.zakenapi.domain")
class ZakenApiAutoConfiguration {

    @Bean
    fun zakenApiClient(
        webclientBuilder: WebClient.Builder
    ): ZakenApiClient {
        return ZakenApiClient(webclientBuilder)
    }

    @Bean
    fun zakenApiPluginFactory(
        pluginService: PluginService,
        zakenApiClient: ZakenApiClient,
        urlProvider: ZaakUrlProvider,
        storageService: TemporaryResourceStorageService,
        zaakInstanceLinkRepository: ZaakInstanceLinkRepository,
    ): ZakenApiPluginFactory {
        return ZakenApiPluginFactory(
            pluginService,
            zakenApiClient,
            urlProvider,
            storageService,
            zaakInstanceLinkRepository,
        )
    }

    @Bean
    fun zakenApiZaakInstanceLinkService(
        zaakInstanceLinkRepository: ZaakInstanceLinkRepository
    ): ZaakInstanceLinkService {
        return ZaakInstanceLinkService(zaakInstanceLinkRepository)
    }

    @Bean
    fun zaakDocumentService(zaakUrlProvider: ZaakUrlProvider, pluginService: PluginService): ZaakDocumentService {
        return ZaakDocumentService(zaakUrlProvider, pluginService)
    }
}
