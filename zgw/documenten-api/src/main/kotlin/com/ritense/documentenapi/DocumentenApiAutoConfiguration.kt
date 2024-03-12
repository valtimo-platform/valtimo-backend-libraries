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

package com.ritense.documentenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.security.DocumentenApiHttpSecurityConfigurer
import com.ritense.documentenapi.service.DocumentDeleteHandler
import com.ritense.documentenapi.service.DocumentenApiColumnDeploymentService
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.documentenapi.web.rest.DocumentenApiManagementResource
import com.ritense.documentenapi.web.rest.DocumentenApiResource
import com.ritense.outbox.OutboxService
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.processlink.mapper.PluginProcessLinkMapper
import com.ritense.valtimo.processlink.service.PluginProcessLinkService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.reactive.function.client.WebClient
import javax.sql.DataSource

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["com.ritense.documentenapi.repository"])
@EntityScan("com.ritense.documentenapi.domain")
class DocumentenApiAutoConfiguration {

    @Bean
    fun documentenApiClient(
        webclientBuilder: WebClient.Builder,
        outboxService: OutboxService,
        objectMapper: ObjectMapper,
        platformTransactionManager: PlatformTransactionManager
    ): DocumentenApiClient {
        return DocumentenApiClient(
            webclientBuilder,
            outboxService,
            objectMapper,
            platformTransactionManager
        )
    }

    @Bean
    fun documentenApiPluginFactory(
        pluginService: PluginService,
        client: DocumentenApiClient,
        storageService: TemporaryResourceStorageService,
        applicationEventPublisher: ApplicationEventPublisher,
        objectMapper: ObjectMapper,
        documentDeleteHandlers: List<DocumentDeleteHandler>
    ): DocumentenApiPluginFactory {
        return DocumentenApiPluginFactory(
            pluginService,
            client,
            storageService,
            applicationEventPublisher,
            objectMapper,
            documentDeleteHandlers
        )
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenApiService::class)
    fun documentenApiService(
        pluginService: PluginService,
        catalogiService: CatalogiService,
        documentenApiColumnRepository: DocumentenApiColumnRepository,
        authorizationService: AuthorizationService,
        documentDefinitionService: JsonSchemaDocumentDefinitionService,
        documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
        pluginProcessLinkService: PluginProcessLinkService,
        camundaRepositoryService: CamundaRepositoryService,
    ): DocumentenApiService {
        return DocumentenApiService(
            pluginService,
            catalogiService,
            documentenApiColumnRepository,
            authorizationService,
            documentDefinitionService,
            documentDefinitionProcessLinkService,
            pluginProcessLinkService,
            camundaRepositoryService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenApiColumnDeploymentService::class)
    fun documentenApiColumnDeploymentService(
        documentenApiService: DocumentenApiService,
    ): DocumentenApiColumnDeploymentService {
        return DocumentenApiColumnDeploymentService(documentenApiService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenApiResource::class)
    fun documentenApiResource(
        documentenApiService: DocumentenApiService
    ): DocumentenApiResource {
        return DocumentenApiResource(documentenApiService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenApiManagementResource::class)
    fun documentenApiManagementResource(
        documentenApiService: DocumentenApiService
    ): DocumentenApiManagementResource {
        return DocumentenApiManagementResource(documentenApiService)
    }

    @Order(380)
    @Bean
    fun documentenApiHttpSecurityConfigurer(): DocumentenApiHttpSecurityConfigurer {
        return DocumentenApiHttpSecurityConfigurer()
    }

    @ConditionalOnClass(DataSource::class)
    @Order(HIGHEST_PRECEDENCE + 32)
    @Bean
    fun documentenApiLiquibaseChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/documenten-api-master.xml")
    }

}
