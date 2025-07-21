/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.klantinteractiesapi

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.klantinteractiesapi.autoconfigure.PartijIdentificatieModule
import com.ritense.klantinteractiesapi.client.KlantinteractiesApiClient
import com.ritense.klantinteractiesapi.ikorepository.KlantinteractiesApiIkoRepository
import com.ritense.plugin.service.PluginService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@AutoConfiguration
class KlantinteractiesApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(KlantinteractiesApiClient::class)
    fun klantinteractiesApiClient(restClientBuilder: RestClient.Builder) = KlantinteractiesApiClient(restClientBuilder)

    @Bean
    @ConditionalOnMissingBean(KlantinteractiesApiPluginFactory::class)
    fun klantinteractiesApiPluginFactory(
        pluginService: PluginService,
        klantinteractiesApiClient: KlantinteractiesApiClient,
    ): KlantinteractiesApiPluginFactory {
        return KlantinteractiesApiPluginFactory(
            pluginService,
            klantinteractiesApiClient,
        )
    }

    @Bean
    @ConditionalOnMissingBean(KlantinteractiesApiIkoRepository::class)
    fun klantinteractiesApiIkoRepository(
        pluginService: PluginService,
        objectMapper: ObjectMapper,
    ): KlantinteractiesApiIkoRepository {
        return KlantinteractiesApiIkoRepository(
            pluginService,
            objectMapper,
        )
    }

    @Bean
    @ConditionalOnMissingBean(PartijIdentificatieModule::class)
    fun partijIdentificatieModule(): Module {
        return PartijIdentificatieModule()
    }
}