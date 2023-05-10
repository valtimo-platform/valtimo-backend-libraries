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

package com.ritense.besluit.autoconfigure

import com.ritense.besluit.client.BesluitClient
import com.ritense.besluit.client.BesluitTokenGenerator
import com.ritense.besluit.connector.BesluitConnector
import com.ritense.besluit.connector.BesluitProperties
import com.ritense.besluit.listener.BesluitServiceTaskListener
import com.ritense.besluit.service.BesluitService
import com.ritense.besluit.web.rest.BesluitResource
import com.ritense.connector.service.ConnectorService
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.catalogi.CatalogiClient
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.resource.service.OpenZaakService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class BesluitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BesluitClient::class)
    fun besluitClient(
        webclientBuilder: WebClient.Builder,
        besluitTokenGenerator: BesluitTokenGenerator,
    ): BesluitClient {
        return BesluitClient(webclientBuilder, besluitTokenGenerator)
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
        besluitClient: BesluitClient,
        applicationEventPublisher: ApplicationEventPublisher
    ) : BesluitConnector {
        return BesluitConnector(besluitProperties, besluitClient, applicationEventPublisher)
    }

    @Bean
    @ConditionalOnMissingBean(BesluitProperties::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun besluitProperties() : BesluitProperties {
        return BesluitProperties()
    }

    @Bean
    @ConditionalOnMissingBean(BesluitResource::class)
    fun besluitResource(
        besluitService: BesluitService,
    ): BesluitResource {
        return com.ritense.besluit.web.rest.impl.BesluitResource(besluitService)
    }

    // Services

    @Bean
    @ConditionalOnMissingBean(BesluitService::class)
    fun besluitService(
        catalogiClient: CatalogiClient,
        connectorService: ConnectorService,
    ): BesluitService {
        return BesluitService(catalogiClient, connectorService)
    }

    @Bean
    @ConditionalOnMissingBean(BesluitServiceTaskListener::class)
    fun besluitServiceTaskListener(
        zaakTypeLinkService: ZaakTypeLinkService,
        documentService: DocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        repositoryService: RepositoryService,
        connectorService: ConnectorService,
        openZaakService: OpenZaakService,
        @Value("\${valtimo.besluitDocumentRequired:false}") besluitDocumentRequired: Boolean,
    ): BesluitServiceTaskListener {
        return BesluitServiceTaskListener(
            zaakTypeLinkService,
            documentService,
            zaakInstanceLinkService,
            repositoryService,
            connectorService,
            openZaakService,
            besluitDocumentRequired
        )
    }

}
