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

package com.ritense.besluit.autoconfigure

import com.ritense.besluit.client.BesluitClient
import com.ritense.besluit.client.BesluitTokenGenerator
import com.ritense.besluit.connector.BesluitConnector
import com.ritense.besluit.connector.BesluitProperties
import io.netty.handler.logging.LogLevel
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.besluit.repository"])
@EntityScan("com.ritense.besluit.domain")
class BesluitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebClient::class)
    fun contactMomentWebClientBuilder(): WebClient {
        return WebClient.builder().clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().wiretap(
                    "reactor.netty.http.client.HttpClient",
                    LogLevel.DEBUG,
                    AdvancedByteBufFormat.TEXTUAL
                )
            )
        ).build()
    }

    @Bean
    @ConditionalOnMissingBean(BesluitClient::class)
    fun besluitenService(
        besluitWebClient: WebClient,
        besluitTokenGenerator: BesluitTokenGenerator,
    ): BesluitClient {
        return BesluitClient(besluitWebClient, besluitTokenGenerator)
    }

    @Bean
    @ConditionalOnMissingBean(BesluitTokenGenerator::class)
    fun besluitTokenGenerator() : BesluitTokenGenerator {
        return BesluitTokenGenerator()
    }

    // Connector

    @Bean
    @ConditionalOnMissingBean(BesluitConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun besluitConnector(
        besluitProperties: BesluitProperties,
        besluitClient: BesluitClient
    ) : BesluitConnector {
        return BesluitConnector(besluitProperties, besluitClient)
    }

    @Bean
    @ConditionalOnMissingBean(BesluitProperties::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun besluitProperties() : BesluitProperties {
        return BesluitProperties()
    }

    // Services

}