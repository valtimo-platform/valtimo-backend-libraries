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

package com.ritense.mail.wordpressmail.autoconfigure

import com.ritense.connector.domain.Connector
import com.ritense.connector.service.ConnectorService
import com.ritense.mail.MailDispatcher
import com.ritense.mail.wordpressmail.connector.WordpressMailConnector
import com.ritense.mail.wordpressmail.connector.WordpressMailConnectorProperties
import com.ritense.mail.wordpressmail.service.WordpressMailClient
import com.ritense.mail.wordpressmail.service.WordpressMailDispatcher
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@Configuration
@EnableConfigurationProperties
class WordpressMailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MailDispatcher::class)
    fun wordpressMailDispatcher(
        connectorService: ConnectorService
    ): WordpressMailDispatcher {
        return WordpressMailDispatcher(connectorService)
    }

    @Bean
    @ConditionalOnMissingBean(WordpressMailClient::class)
    fun wordpressMailClient(
        wordpressMailConnectorProperties: WordpressMailConnectorProperties,
        wordpressMailWebClientBuilder: WebClient.Builder
    ): WordpressMailClient {
        return WordpressMailClient(wordpressMailConnectorProperties, wordpressMailWebClientBuilder)
    }

    //Connector

    @Bean
    @ConditionalOnMissingBean(WordpressMailConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun wordpressMailConnector(
        wordpressMailConnectorProperties: WordpressMailConnectorProperties,
        wordpressMailClient: WordpressMailClient,
        applicationEventPublisher: ApplicationEventPublisher
    ): Connector {
        return WordpressMailConnector(wordpressMailConnectorProperties, wordpressMailClient, applicationEventPublisher)
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun wordpressMailConnectorProperties(): WordpressMailConnectorProperties {
        return WordpressMailConnectorProperties()
    }

}