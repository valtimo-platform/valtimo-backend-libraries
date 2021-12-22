/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.contactmoment.autoconfigure

import com.ritense.connector.domain.Connector
import com.ritense.contactmoment.client.ContactMomentClient
import com.ritense.contactmoment.client.ContactMomentTokenGenerator
import com.ritense.contactmoment.connector.ContactMomentConnector
import com.ritense.contactmoment.connector.ContactMomentProperties
import io.netty.handler.logging.LogLevel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@SpringBootConfiguration
@Configuration
class ContactMomentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebClient.Builder::class)
    fun contactMomentWebClientBuilder(): WebClient.Builder {
        return WebClient.builder().clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().wiretap(
                    "reactor.netty.http.client.HttpClient",
                    LogLevel.DEBUG,
                    AdvancedByteBufFormat.TEXTUAL
                )
            )
        )
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
    }

    @Bean
    @ConditionalOnMissingBean(ContactMomentClient::class)
    fun contactMomentClient(
        contactMomentWebClientBuilder: WebClient.Builder,
        contactMomentTokenGenerator: ContactMomentTokenGenerator,
    ): ContactMomentClient {
        return ContactMomentClient(contactMomentWebClientBuilder, contactMomentTokenGenerator)
    }

    @Bean
    @ConditionalOnMissingBean(ContactMomentTokenGenerator::class)
    fun contactMomentTokenGenerator(): ContactMomentTokenGenerator {
        return ContactMomentTokenGenerator()
    }

    @Bean
    @ConditionalOnMissingBean(ContactMomentConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun contactMomentConnector(
        contactMomentProperties: ContactMomentProperties,
        contactMomentClient: ContactMomentClient,
    ): Connector {
        return ContactMomentConnector(contactMomentProperties, contactMomentClient)
    }

    @Bean
    @ConditionalOnMissingBean(ContactMomentProperties::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun contactMomentProperties(
    ): ContactMomentProperties {
        return ContactMomentProperties()
    }

}
