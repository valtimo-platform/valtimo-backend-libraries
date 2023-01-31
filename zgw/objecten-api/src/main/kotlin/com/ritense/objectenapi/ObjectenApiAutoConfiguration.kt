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

package com.ritense.objectenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.form.service.FormDefinitionService
import com.ritense.objectenapi.client.ObjectenApiClient
import com.ritense.objectenapi.listener.ZaakObjectListener
import com.ritense.objectenapi.security.ObjectenApiHttpSecurityConfigurer
import com.ritense.objectenapi.service.ZaakObjectDataResolver
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.plugin.service.PluginService
import io.netty.handler.logging.LogLevel
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

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
    fun objectenApiClient(webclientBuilder: WebClient.Builder): ObjectenApiClient {
        return ObjectenApiClient(webclientBuilder)
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
        zaakInstanceLinkService: ZaakInstanceLinkService,
        pluginService : PluginService,
        formDefinitionService : FormDefinitionService
    ): ZaakObjectService {
        return ZaakObjectService(zaakInstanceLinkService, pluginService, formDefinitionService)
    }

    @Order(400)
    @Bean
    fun objectenApiHttpSecurityConfigurer(): ObjectenApiHttpSecurityConfigurer {
        return ObjectenApiHttpSecurityConfigurer()
    }

    @Bean
    fun zaakObjectDataResolver(
        zaakObjectService: ZaakObjectService,
        objectMapper: ObjectMapper
    ): ZaakObjectDataResolver {
        return ZaakObjectDataResolver(zaakObjectService, objectMapper)
    }
}