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
import com.ritense.iko.IkoServerRepository
import com.ritense.iko.IkoValueResolverFactory
import com.ritense.iko.authorization.IkoDataAggregateSpecificationFactory
import com.ritense.iko.client.IkoClient
import com.ritense.iko.importer.IkoRepositoryConfigImporter
import com.ritense.iko.importer.IkoDataAggregateImporter
import com.ritense.iko.importer.IkoDataRequestImporter
import com.ritense.iko.importer.IkoListColumnImporter
import com.ritense.iko.importer.IkoSearchFieldImporter
import com.ritense.iko.importer.IkoTabImporter
import com.ritense.iko.importer.IkoWidgetImporter
import com.ritense.iko.plugin.IkoPluginFactory
import com.ritense.iko.repository.IkoRepositoryConfigRepository
import com.ritense.iko.repository.IkoDataAggregateListColumnRepository
import com.ritense.iko.repository.IkoDataAggregateRepository
import com.ritense.iko.repository.IkoDataAggregateTabRepository
import com.ritense.iko.repository.IkoDataRequestRepository
import com.ritense.iko.repository.IkoDataRequestSearchFieldRepository
import com.ritense.iko.repository.IkoTabWidgetRepository
import com.ritense.iko.security.config.IkoHttpSecurityConfigurer
import com.ritense.iko.service.IkoRepositoryService
import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.service.IkoListColumnService
import com.ritense.iko.service.IkoSearchFieldService
import com.ritense.iko.service.IkoTabService
import com.ritense.iko.service.IkoWidgetService
import com.ritense.iko.valueresolver.IkoValueResolverServiceImpl
import com.ritense.iko.web.rest.IkoRepositoryManagementResource
import com.ritense.iko.web.rest.IkoDataAggregateManagementResource
import com.ritense.iko.web.rest.IkoDataAggregateResource
import com.ritense.iko.web.rest.IkoDataRequestManagementResource
import com.ritense.iko.web.rest.IkoDataRequestResource
import com.ritense.iko.web.rest.IkoListColumnManagementResource
import com.ritense.iko.web.rest.IkoSearchFieldManagementResource
import com.ritense.iko.web.rest.IkoTabManagementResource
import com.ritense.iko.web.rest.IkoTabResource
import com.ritense.iko.web.rest.IkoWidgetManagementResource
import com.ritense.iko.web.rest.IkoWidgetResource
import com.ritense.plugin.service.PluginService
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import com.ritense.tab.service.TabService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.contract.iko.IkoRepository
import com.ritense.valueresolver.ValueResolverService
import com.ritense.widget.service.WidgetService
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
    @ConditionalOnMissingBean(IkoRepositoryService::class)
    fun ikoRepositoryService(
        ikoRepositoryConfigRepository: IkoRepositoryConfigRepository,
        authorizationService: AuthorizationService,
        ikoRepositories: List<IkoRepository>,
    ) = IkoRepositoryService(
        ikoRepositoryConfigRepository,
        authorizationService,
        ikoRepositories,
    )

    @Bean
    @ConditionalOnMissingBean(IkoDataAggregateService::class)
    fun ikoDataAggregateService(
        ikoDataAggregateRepository: IkoDataAggregateRepository,
        ikoRepositoryService: IkoRepositoryService,
        authorizationService: AuthorizationService,
        ikoRepositories: List<IkoRepository>,
    ) = IkoDataAggregateService(
        ikoDataAggregateRepository,
        ikoRepositoryService,
        authorizationService,
        ikoRepositories
    )

    @Bean
    @ConditionalOnMissingBean(IkoDataRequestService::class)
    fun ikoDataRequestService(
        ikoDataRequestRepository: IkoDataRequestRepository,
        ikoDataAggregateService: IkoDataAggregateService,
        authorizationService: AuthorizationService,
        ikoRepositories: List<IkoRepository>,
    ) = IkoDataRequestService(
        ikoDataRequestRepository,
        ikoDataAggregateService,
        authorizationService,
        ikoRepositories,
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

    @Order(HIGHEST_PRECEDENCE + 35)
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
    @ConditionalOnMissingBean(IkoRepositoryManagementResource::class)
    fun ikoRepositoryManagementResource(
        service: IkoRepositoryService,
    ): IkoRepositoryManagementResource {
        return IkoRepositoryManagementResource(
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
        listColumnService: IkoListColumnService,
        searchFieldService: IkoSearchFieldService,
    ): IkoDataRequestResource {
        return IkoDataRequestResource(
            dataRequestService,
            listColumnService,
            searchFieldService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoWidgetResource::class)
    fun ikoWidgetResource(
        ikoWidgetService: IkoWidgetService,
    ): IkoWidgetResource {
        return IkoWidgetResource(
            ikoWidgetService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoTabResource::class)
    fun ikoTabResource(
        ikoTabService: IkoTabService,
    ): IkoTabResource {
        return IkoTabResource(
            ikoTabService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoTabManagementResource::class)
    fun ikoTabManagementResource(
        ikoTabService: IkoTabService,
    ): IkoTabManagementResource {
        return IkoTabManagementResource(
            ikoTabService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoWidgetManagementResource::class)
    fun ikoWidgetManagementResource(
        ikoWidgetService: IkoWidgetService,
    ): IkoWidgetManagementResource {
        return IkoWidgetManagementResource(
            ikoWidgetService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoListColumnManagementResource::class)
    fun ikoListColumnManagementResource(
        ikoListColumnService: IkoListColumnService,
    ): IkoListColumnManagementResource {
        return IkoListColumnManagementResource(
            ikoListColumnService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoSearchFieldManagementResource::class)
    fun ikoSearchFieldManagementResource(
        ikoSearchFieldService: IkoSearchFieldService,
    ): IkoSearchFieldManagementResource {
        return IkoSearchFieldManagementResource(
            ikoSearchFieldService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoClient::class)
    fun ikoClient(
        restClientBuilder: RestClient.Builder,
    ): IkoClient {
        return IkoClient(
            restClientBuilder,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoValueResolverFactory::class)
    fun ikoValueResolverFactory(
        ikoDataAggregateService: IkoDataAggregateService,
        ikoDataRequestService: IkoDataRequestService,
        searchFieldService: IkoSearchFieldService,
        objectMapper: ObjectMapper,
    ): IkoValueResolverFactory {
        return IkoValueResolverFactory(
            ikoDataAggregateService,
            ikoDataRequestService,
            searchFieldService,
            objectMapper,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoServerRepository::class)
    fun ikoServerRepository(
        pluginService: PluginService,
    ): IkoServerRepository {
        return IkoServerRepository(
            pluginService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoRepositoryConfigImporter::class)
    fun ikoRepositoryConfigImporter(
        objectMapper: ObjectMapper,
        ikoRepositoryService: IkoRepositoryService,
    ): IkoRepositoryConfigImporter {
        return IkoRepositoryConfigImporter(
            objectMapper,
            ikoRepositoryService,
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
        searchFieldService: IkoSearchFieldService,
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
        listColumnService: IkoListColumnService,
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
        ikoTabService: IkoTabService,
    ): IkoTabImporter {
        return IkoTabImporter(
            objectMapper,
            ikoTabService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoWidgetImporter::class)
    fun ikoWidgetImporter(
        objectMapper: ObjectMapper,
        ikoWidgetService: IkoWidgetService,
    ): IkoWidgetImporter {
        return IkoWidgetImporter(
            objectMapper,
            ikoWidgetService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoWidgetService::class)
    fun ikoWidgetService(
        ikoTabService: IkoTabService,
        ikoTabWidgetRepository: IkoTabWidgetRepository,
        widgetService: WidgetService,
    ): IkoWidgetService {
        return IkoWidgetService(
            ikoTabService,
            ikoTabWidgetRepository,
            widgetService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoTabService::class)
    fun ikoTabService(
        tabService: TabService,
        ikoDataAggregateTabRepository: IkoDataAggregateTabRepository,
    ): IkoTabService {
        return IkoTabService(
            tabService,
            ikoDataAggregateTabRepository,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoListColumnService::class)
    fun ikoListColumnService(
        listColumnService: SearchListColumnService,
        ikoDataAggregateListColumnRepository: IkoDataAggregateListColumnRepository,
    ): IkoListColumnService {
        return IkoListColumnService(
            listColumnService,
            ikoDataAggregateListColumnRepository,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoSearchFieldService::class)
    fun ikoSearchFieldService(
        searchFieldService: SearchFieldV2Service,
        ikoDataRequestSearchFieldRepository: IkoDataRequestSearchFieldRepository,
    ): IkoSearchFieldService {
        return IkoSearchFieldService(
            searchFieldService,
            ikoDataRequestSearchFieldRepository,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoValueResolverServiceImpl::class)
    fun ikoValueResolverService(
        valueResolverService: ValueResolverService,
        valueResolverFactories: List<IkoValueResolverFactory>,
    ): IkoValueResolverServiceImpl {
        return IkoValueResolverServiceImpl(
            valueResolverService,
            valueResolverFactories,
        )
    }

    @Bean
    @ConditionalOnMissingBean(IkoPluginFactory::class)
    fun ikoPluginFactory(
        pluginService: PluginService,
        ikoClient: IkoClient,
    ): IkoPluginFactory {
        return IkoPluginFactory(
            pluginService,
            ikoClient,
        )
    }

}