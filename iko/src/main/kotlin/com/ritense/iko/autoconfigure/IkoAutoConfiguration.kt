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

package com.ritense.iko.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.iko.IkoApiConnector
import com.ritense.iko.IkoValueResolverFactory
import com.ritense.iko.authorization.IkoDataAggregateSpecificationFactory
import com.ritense.iko.client.IkoApiClient
import com.ritense.iko.importer.IkoConnectorConfigImporter
import com.ritense.iko.importer.IkoDataAggregateImporter
import com.ritense.iko.importer.IkoDataRequestImporter
import com.ritense.iko.importer.IkoListColumnImporter
import com.ritense.iko.importer.IkoSearchFieldImporter
import com.ritense.iko.importer.IkoTabImporter
import com.ritense.iko.repository.IkoConnectorConfigRepository
import com.ritense.iko.repository.IkoDataAggregateRepository
import com.ritense.iko.repository.IkoDataRequestRepository
import com.ritense.iko.security.config.IkoHttpSecurityConfigurer
import com.ritense.iko.service.IkoConnectorService
import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.web.rest.IkoConnectorManagementResource
import com.ritense.iko.web.rest.IkoDataAggregateManagementResource
import com.ritense.iko.web.rest.IkoDataAggregateResource
import com.ritense.iko.web.rest.IkoDataRequestManagementResource
import com.ritense.iko.web.rest.IkoDataRequestResource
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import com.ritense.tab.service.TabService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.contract.iko.IkoConnector
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.client.RestClient

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["com.ritense.iko.repository"])
@EntityScan("com.ritense.iko.domain")
class IkoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IkoConnectorService::class)
    fun ikoConnectorService(
        ikoConnectorConfigRepository: IkoConnectorConfigRepository,
        authorizationService: AuthorizationService,
        ikoConnectors: List<IkoConnector>,
    ) = IkoConnectorService(
        ikoConnectorConfigRepository,
        authorizationService,
        ikoConnectors,
    )

    @Bean
    @ConditionalOnMissingBean(IkoDataAggregateService::class)
    fun ikoDataAggregateService(
        ikoDataAggregateRepository: IkoDataAggregateRepository,
        ikoConnectorService: IkoConnectorService,
        authorizationService: AuthorizationService,
        ikoConnectors: List<IkoConnector>,
    ) = IkoDataAggregateService(
        ikoDataAggregateRepository,
        ikoConnectorService,
        authorizationService,
        ikoConnectors
    )

    @Bean
    @ConditionalOnMissingBean(IkoDataRequestService::class)
    fun ikoDataRequestService(
        ikoDataRequestRepository: IkoDataRequestRepository,
        ikoDataAggregateService: IkoDataAggregateService,
        authorizationService: AuthorizationService,
        ikoConnectors: List<IkoConnector>,
    ) = IkoDataRequestService(
        ikoDataRequestRepository,
        ikoDataAggregateService,
        authorizationService,
        ikoConnectors,
    )

    @Order(300)
    @Bean
    @ConditionalOnMissingBean(IkoHttpSecurityConfigurer::class)
    fun ikoHttpSecurityConfigurer(): IkoHttpSecurityConfigurer {
        return IkoHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(IkoDataAggregateSpecificationFactory::class)
    fun ikoDataAggregateSpecificationFactory(
        ikoDataAggregateRepository: IkoDataAggregateRepository,
        queryDialectHelper: QueryDialectHelper,
    ): IkoDataAggregateSpecificationFactory {
        return IkoDataAggregateSpecificationFactory(
            ikoDataAggregateRepository,
            queryDialectHelper,
        )
    }

    @Order(HIGHEST_PRECEDENCE + 34)
    @Bean
    @ConditionalOnMissingBean(name = ["ikoLiquibaseMasterChangeLogLocation"])
    fun ikoLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/iko-master.xml")
    }

    @Bean
    @ConditionalOnMissingBean(IkoDataAggregateResource::class)
    fun ikoDataAggregateResource(
        service: IkoDataAggregateService,
    ): IkoDataAggregateResource {
        return IkoDataAggregateResource(
            service,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoConnectorManagementResource::class)
    fun ikoConnectorManagementResource(
        service: IkoConnectorService,
    ): IkoConnectorManagementResource {
        return IkoConnectorManagementResource(
            service,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoDataAggregateManagementResource::class)
    fun ikoDataAggregateManagementResource(
        service: IkoDataAggregateService,
    ): IkoDataAggregateManagementResource {
        return IkoDataAggregateManagementResource(
            service,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoDataRequestManagementResource::class)
    fun ikoDataRequestManagementResource(
        service: IkoDataRequestService,
    ): IkoDataRequestManagementResource {
        return IkoDataRequestManagementResource(
            service,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoDataRequestResource::class)
    fun ikoDataRequestResource(
        dataRequestService: IkoDataRequestService,
        listColumnService: SearchListColumnService,
        searchFieldService: SearchFieldV2Service,
    ): IkoDataRequestResource {
        return IkoDataRequestResource(
            dataRequestService,
            listColumnService,
            searchFieldService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoApiClient::class)
    fun ikoApiClient(
        restClientBuilder: RestClient.Builder,
    ): IkoApiClient {
        return IkoApiClient(
            restClientBuilder,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoValueResolverFactory::class)
    fun ikoValueResolverFactory(
        ikoDataAggregateService: IkoDataAggregateService,
    ): IkoValueResolverFactory {
        return IkoValueResolverFactory(
            ikoDataAggregateService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoApiConnector::class)
    fun ikoApiConnector(
        ikoApiClient: IkoApiClient,
    ): IkoApiConnector {
        return IkoApiConnector(
            ikoApiClient,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoConnectorConfigImporter::class)
    fun ikoConnectorConfigImporter(
        objectMapper: ObjectMapper,
        ikoConnectorService: IkoConnectorService,
    ): IkoConnectorConfigImporter {
        return IkoConnectorConfigImporter(
            objectMapper,
            ikoConnectorService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoDataAggregateImporter::class)
    fun ikoDataAggregateImporter(
        objectMapper: ObjectMapper,
        ikoDataAggregateService: IkoDataAggregateService,
    ): IkoDataAggregateImporter {
        return IkoDataAggregateImporter(
            objectMapper,
            ikoDataAggregateService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoDataRequestImporter::class)
    fun ikoDataRequestImporter(
        objectMapper: ObjectMapper,
        ikoDataRequestService: IkoDataRequestService,
    ): IkoDataRequestImporter {
        return IkoDataRequestImporter(
            objectMapper,
            ikoDataRequestService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoSearchFieldImporter::class)
    fun ikoSearchFieldImporter(
        objectMapper: ObjectMapper,
        searchFieldService: SearchFieldV2Service,
    ): IkoSearchFieldImporter {
        return IkoSearchFieldImporter(
            objectMapper,
            searchFieldService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoListColumnImporter::class)
    fun ikoListColumnImporter(
        objectMapper: ObjectMapper,
        listColumnService: SearchListColumnService,
    ): IkoListColumnImporter {
        return IkoListColumnImporter(
            objectMapper,
            listColumnService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoTabImporter::class)
    fun ikoTabImporter(
        objectMapper: ObjectMapper,
        tabService: TabService,
    ): IkoTabImporter {
        return IkoTabImporter(
            objectMapper,
            tabService,
        )
    }

}