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

package com.ritense.mail.denhaagmailer.autoconfigure

import com.ritense.connector.domain.Connector
import com.ritense.connector.service.ConnectorService
import com.ritense.mail.MailDispatcher
import com.ritense.mail.denhaagmailer.connector.DenHaagMailerConnector
import com.ritense.mail.denhaagmailer.connector.DenHaagMailerConnectorProperties
import com.ritense.mail.denhaagmailer.service.DenHaagMailClient
import com.ritense.mail.denhaagmailer.service.DenHaagMailDispatcher
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@Configuration
@EnableConfigurationProperties
class DenHaagMailerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MailDispatcher::class)
    fun denHaagMailDispatcher(
        connectorService: ConnectorService
    ): DenHaagMailDispatcher {
        return DenHaagMailDispatcher(connectorService)
    }

    @Bean
    @ConditionalOnMissingBean(DenHaagMailClient::class)
    fun denHaagMailClient(
        denHaagMailerConnectorProperties: DenHaagMailerConnectorProperties,
        denHaagMailerWebClientBuilder: WebClient.Builder
    ): DenHaagMailClient {
        return DenHaagMailClient(denHaagMailerConnectorProperties, denHaagMailerWebClientBuilder)
    }

    @Bean
    @ConditionalOnMissingBean(WebClient.Builder::class)
    fun denHaagMailerWebClientBuilder(): WebClient.Builder {
        return WebClient.builder().clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().wiretap(
                    "reactor.netty.http.client.HttpClient",
                    io.netty.handler.logging.LogLevel.DEBUG,
                    AdvancedByteBufFormat.TEXTUAL
                )
            )
        )
    }

    //Connector

    @Bean
    @ConditionalOnMissingBean(DenHaagMailerConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun denHaagMailerConnector(
        denHaagMailerConnectorProperties: DenHaagMailerConnectorProperties,
        denHaagMailClient: DenHaagMailClient
    ): Connector {
        return DenHaagMailerConnector(denHaagMailerConnectorProperties, denHaagMailClient)
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun denHaagMailerConnectorProperties(): DenHaagMailerConnectorProperties {
        return DenHaagMailerConnectorProperties()
    }

}