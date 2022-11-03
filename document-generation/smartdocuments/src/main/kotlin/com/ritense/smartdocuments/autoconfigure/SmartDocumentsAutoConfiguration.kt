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

package com.ritense.smartdocuments.autoconfigure

import com.ritense.connector.domain.Connector
import com.ritense.connector.service.ConnectorService
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.resource.service.ResourceService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.connector.SmartDocumentsConnector
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.security.config.SmartDocumentsHttpSecurityConfigurer
import com.ritense.smartdocuments.service.CamundaSmartDocumentGenerator
import com.ritense.smartdocuments.service.SmartDocumentGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.core.annotation.Order
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@Configuration
class SmartDocumentsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CamundaSmartDocumentGenerator::class)
    fun camundaSmartDocumentGenerator(
        smartDocumentGenerator: SmartDocumentGenerator,
        processDocumentAssociationService: ProcessDocumentAssociationService,
        documentService: DocumentService,
    ): CamundaSmartDocumentGenerator {
        return CamundaSmartDocumentGenerator(
            smartDocumentGenerator,
            processDocumentAssociationService,
            documentService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(SmartDocumentGenerator::class)
    fun smartDocumentGenerator(
        connectorService: ConnectorService,
        documentService: DocumentService,
        resourceService: ResourceService,
        applicationEventPublisher: ApplicationEventPublisher,
    ): SmartDocumentGenerator {
        return SmartDocumentGenerator(
            connectorService,
            documentService,
            resourceService,
            applicationEventPublisher,
        )
    }

    @Bean
    @ConditionalOnMissingBean(SmartDocumentsClient::class)
    fun smartDocumentsClient(
        smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
        smartDocumentsWebClientBuilder: WebClient.Builder,
        @Value("\${valtimo.smartdocuments.max-file-size-mb:10}") maxFileSize: Int,
        temporaryResourceStorageService: TemporaryResourceStorageService,
    ): SmartDocumentsClient {
        return SmartDocumentsClient(
            smartDocumentsConnectorProperties,
            smartDocumentsWebClientBuilder,
            maxFileSize,
            temporaryResourceStorageService
        )
    }

    @Bean
    @ConditionalOnMissingBean(WebClient.Builder::class)
    fun smartDocumentsWebClientBuilder(): WebClient.Builder {
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
    @ConditionalOnMissingBean(SmartDocumentsConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun smartDocumentsConnector(
        smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
        smartDocumentsClient: SmartDocumentsClient
    ): Connector {
        return SmartDocumentsConnector(
            smartDocumentsConnectorProperties,
            smartDocumentsClient
        )
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun smartDocumentsConnectorProperties(): SmartDocumentsConnectorProperties {
        return SmartDocumentsConnectorProperties()
    }

    @Order(480)
    @Bean
    @ConditionalOnMissingBean(SmartDocumentsHttpSecurityConfigurer::class)
    fun smartDocumentsHttpSecurityConfigurer(): SmartDocumentsHttpSecurityConfigurer {
        return SmartDocumentsHttpSecurityConfigurer()
    }
}
