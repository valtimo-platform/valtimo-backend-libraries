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

package com.ritense.zakenapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.outbox.OutboxService
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPluginFactory
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import com.ritense.zakenapi.provider.BsnProvider
import com.ritense.zakenapi.provider.KvkProvider
import com.ritense.zakenapi.provider.DefaultZaakUrlProvider
import com.ritense.zakenapi.provider.DefaultZaaktypeUrlProvider
import com.ritense.zakenapi.provider.ZaakBsnProvider
import com.ritense.zakenapi.provider.ZaakKvkProvider
import com.ritense.zakenapi.repository.ZaakHersteltermijnRepository
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import com.ritense.zakenapi.repository.ZaakTypeLinkRepository
import com.ritense.zakenapi.resolver.ZaakStatusValueResolverFactory
import com.ritense.zakenapi.resolver.ZaakValueResolverFactory
import com.ritense.zakenapi.security.ZakenApiHttpSecurityConfigurer
import com.ritense.zakenapi.service.DefaultZaakTypeLinkService
import com.ritense.zakenapi.service.ZaakDocumentService
import com.ritense.zakenapi.service.ZaakTypeLinkService
import com.ritense.zakenapi.service.ZakenApiEventListener
import com.ritense.zakenapi.web.rest.DefaultZaakTypeLinkResource
import com.ritense.zakenapi.web.rest.ZaakDocumentResource
import com.ritense.zakenapi.web.rest.ZaakTypeLinkResource
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.reactive.function.client.WebClient
import kotlin.contracts.ExperimentalContracts

@AutoConfiguration
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
        zaakHersteltermijnRepository: ZaakHersteltermijnRepository,
        platformTransactionManager: PlatformTransactionManager,
    ): ZakenApiPluginFactory {
        return ZakenApiPluginFactory(
            pluginService,
            zakenApiClient,
            urlProvider,
            storageService,
            zaakInstanceLinkRepository,
            zaakHersteltermijnRepository,
            platformTransactionManager,
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

    @OptIn(ExperimentalContracts::class)
    @Bean
    @ConditionalOnMissingBean(BsnProvider::class)
    fun bsnProvider(
        processDocumentService: ProcessDocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        pluginService: PluginService
    ): BsnProvider {
        return ZaakBsnProvider(
            processDocumentService,
            zaakInstanceLinkService,
            pluginService
        )
    }

    @OptIn(ExperimentalContracts::class)
    @Bean
    @ConditionalOnMissingBean(KvkProvider::class)
    fun kvkProvider(
        processDocumentService: ProcessDocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        pluginService: PluginService
    ) : KvkProvider {
        return ZaakKvkProvider(
            processDocumentService,
            zaakInstanceLinkService,
            pluginService
        )
    }

    @Order(300)
    @Bean
    fun zakenApiHttpSecurityConfigurer(): ZakenApiHttpSecurityConfigurer {
        return ZakenApiHttpSecurityConfigurer()
    }

    @Bean
    fun zakenApiZaakTypeLinkService(
        zaakTypeLinkRepository: ZaakTypeLinkRepository,
        processDocumentAssociationService: ProcessDocumentAssociationService
    ): ZaakTypeLinkService {
        return DefaultZaakTypeLinkService(zaakTypeLinkRepository, processDocumentAssociationService)
    }

    @Bean
    fun zakenApiEventListener(
        pluginService: PluginService,
        zaakTypeLinkService: ZaakTypeLinkService
    ): ZakenApiEventListener {
        return ZakenApiEventListener(pluginService, zaakTypeLinkService)
    }

    @Bean
    fun zakenApiZaakTypeLinkResource(
        zaakTypeLinkService: ZaakTypeLinkService
    ): ZaakTypeLinkResource {
        return DefaultZaakTypeLinkResource(zaakTypeLinkService)
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(ZaakUrlProvider::class)
    fun zaakUrlProvider(zaakInstanceLinkService: ZaakInstanceLinkService): ZaakUrlProvider {
        return DefaultZaakUrlProvider(zaakInstanceLinkService)
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(ZaaktypeUrlProvider::class)
    fun zaaktypeUrlProvider(zaakTypeLinkService: ZaakTypeLinkService): ZaaktypeUrlProvider {
        return DefaultZaaktypeUrlProvider(zaakTypeLinkService)
    }
}
