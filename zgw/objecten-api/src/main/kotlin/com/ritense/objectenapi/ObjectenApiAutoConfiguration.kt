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

package com.ritense.objectenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.form.service.FormDefinitionService
import com.ritense.objectenapi.client.ObjectenApiClient
import com.ritense.objectenapi.listener.ZaakObjectListener
import com.ritense.objectenapi.management.ErrorObjectManagementInfoProvider
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objectenapi.security.ObjectenApiHttpSecurityConfigurer
import com.ritense.objectenapi.service.ZaakObjectDataResolver
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.objectenapi.service.ZaakObjectValueResolverFactory
import com.ritense.objectenapi.web.rest.ObjectResource
import com.ritense.objectenapi.web.rest.ZaakObjectResource
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.http.WebClientBuilderHolder
import com.ritense.zakenapi.ZaakUrlProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Configuration
class ObjectenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ZaakObjectListener::class)
    fun formSubmissionListener(
        pluginService: PluginService,
        zaakObjectService: ZaakObjectService
    ): ZaakObjectListener {
        return ZaakObjectListener(pluginService, zaakObjectService)
    }

    @Bean
    @ConditionalOnMissingBean(ObjectenApiClient::class)
    fun objectenApiClient(): ObjectenApiClient {
        return ObjectenApiClient(WebClientBuilderHolder.get())
    }

    @Bean
    fun objectenApiPluginFactory(
        pluginService: PluginService,
        objectenApiClient: ObjectenApiClient
    ): ObjectenApiPluginFactory {
        return ObjectenApiPluginFactory(
            pluginService,
            objectenApiClient
        )
    }

    @Bean
    fun zaakObjectService(
        zaakUrlProvider: ZaakUrlProvider,
        pluginService: PluginService,
        formDefinitionService: FormDefinitionService,
        objectManagementInfoProvider: ObjectManagementInfoProvider
    ): ZaakObjectService {
        return ZaakObjectService(zaakUrlProvider,
            pluginService,
            formDefinitionService,
            objectManagementInfoProvider
        )
    }

    @Order(380)
    @Bean
    fun objectenApiHttpSecurityConfigurer(): ObjectenApiHttpSecurityConfigurer {
        return ObjectenApiHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(ZaakObjectDataResolver::class)
    fun zaakObjectDataResolver(
        zaakObjectService: ZaakObjectService,
        objectMapper: ObjectMapper
    ): ZaakObjectDataResolver {
        return ZaakObjectDataResolver(zaakObjectService, objectMapper)
    }

    @DependsOn("valueResolverService")
    @Bean
    @ConditionalOnMissingBean(ZaakObjectValueResolverFactory::class)
    fun zaakObjectValueResolverFactory(
        zaakObjectService: ZaakObjectService,
        objectMapper: ObjectMapper,
        processDocumentService: ProcessDocumentService,
    ): ZaakObjectValueResolverFactory {
        return ZaakObjectValueResolverFactory(zaakObjectService, objectMapper, processDocumentService)
    }

    @Bean
    @ConditionalOnMissingBean(ObjectResource::class)
    fun objectResource(zaakObjectService: ZaakObjectService): ObjectResource {
        return ObjectResource(zaakObjectService)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakObjectResource::class)
    fun zaakObjectResource(
        zaakObjectService: ZaakObjectService,
        pluginService: PluginService,
    ): ZaakObjectResource {
        return ZaakObjectResource(zaakObjectService, pluginService)
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    @ConditionalOnMissingBean(ObjectManagementInfoProvider::class)
    fun errorObjectManagementInfoProvider(): ObjectManagementInfoProvider {
        return ErrorObjectManagementInfoProvider()
    }
}
