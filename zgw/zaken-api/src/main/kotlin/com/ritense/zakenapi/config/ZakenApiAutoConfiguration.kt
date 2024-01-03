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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.OutboxService
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPluginFactory
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zakenapi.resolver.ZaakStatusValueResolverFactory
import com.ritense.zakenapi.resolver.ZaakValueResolverFactory
import com.ritense.zakenapi.security.ZakenApiHttpSecurityConfigurer
import com.ritense.zakenapi.service.ZaakDocumentService
import com.ritense.zakenapi.web.rest.ZaakDocumentResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.zakenapi.repository"])
@EntityScan("com.ritense.zakenapi.domain")
class ZakenApiAutoConfiguration {

    @Bean
    fun zakenApiClient(
        webclientBuilder: WebClient.Builder,
        outboxService: OutboxService,
        objectMapper: ObjectMapper
    ): ZakenApiClient {
        return ZakenApiClient(webclientBuilder, outboxService, objectMapper)
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

    @Bean
    @ConditionalOnMissingBean(ZaakDocumentResource::class)
    fun zaakDocumentResource(
        zaakDocumentService: ZaakDocumentService
    ): ZaakDocumentResource {
        return ZaakDocumentResource(zaakDocumentService)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakValueResolverFactory::class)
    fun zaakValueResolverFactory(
        zaakDocumentService: ZaakDocumentService,
        processDocumentService: ProcessDocumentService
    ): ZaakValueResolverFactory {
        return ZaakValueResolverFactory(
            zaakDocumentService,
            processDocumentService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ZaakStatusValueResolverFactory::class)
    fun zaakStatusValueResolverFactory(
        processDocumentService: ProcessDocumentService,
        zaakUrlProvider: ZaakUrlProvider,
        pluginService: PluginService,
    ): ZaakStatusValueResolverFactory {
        return ZaakStatusValueResolverFactory(
            processDocumentService,
            zaakUrlProvider,
            pluginService
        )
    }

    @Order(300)
    @Bean
    fun zakenApiHttpSecurityConfigurer(): ZakenApiHttpSecurityConfigurer {
        return ZakenApiHttpSecurityConfigurer()
    }
}
