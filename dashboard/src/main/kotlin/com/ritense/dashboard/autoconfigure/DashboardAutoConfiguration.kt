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

package com.ritense.dashboard.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.dashboard.datasource.WidgetDataSourceResolver
import com.ritense.dashboard.deployment.DashboardDeployer
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.security.config.DashboardHttpSecurityConfigurer
import com.ritense.dashboard.service.DashboardDataService
import com.ritense.dashboard.service.DashboardService
import com.ritense.dashboard.web.rest.AdminDashboardResource
import com.ritense.dashboard.web.rest.DashboardResource
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.dashboard.repository"])
@EntityScan("com.ritense.dashboard.domain")
@EnableCaching
class DashboardAutoConfiguration {

    @Order(HIGHEST_PRECEDENCE + 29)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["dashboardLiquibaseMasterChangeLogLocation"])
    fun dashboardLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/dashboard-master.xml")
    }

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(DashboardHttpSecurityConfigurer::class)
    fun dashboardHttpSecurityConfigurer(): DashboardHttpSecurityConfigurer {
        return DashboardHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(DashboardService::class)
    fun dashboardService(
        applicationContext: ApplicationContext,
        dashboardRepository: DashboardRepository,
        widgetConfigurationRepository: WidgetConfigurationRepository,
        userManagementService: UserManagementService,
        widgetDataSourceResolver: WidgetDataSourceResolver,
    ): DashboardService {
        return DashboardService(
            applicationContext,
            dashboardRepository,
            widgetConfigurationRepository,
            userManagementService,
            widgetDataSourceResolver
        )
    }

    @Bean
    @ConditionalOnMissingBean(DashboardDataService::class)
    fun dashboardDataService(
        applicationContext: ApplicationContext,
        widgetDataSourceResolver: WidgetDataSourceResolver,
        widgetConfigurationRepository: WidgetConfigurationRepository,
        objectMapper: ObjectMapper
    ): DashboardDataService {
        return DashboardDataService(
            applicationContext,
            widgetDataSourceResolver,
            widgetConfigurationRepository,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnMissingBean(AdminDashboardResource::class)
    fun adminDashboardResource(
        dashboardService: DashboardService
    ): AdminDashboardResource {
        return AdminDashboardResource(dashboardService)
    }

    @Bean
    @ConditionalOnMissingBean(DashboardResource::class)
    fun dashboardResource(
        dashboardService: DashboardService,
        dashboardDataService: DashboardDataService
    ): DashboardResource {
        return DashboardResource(dashboardService, dashboardDataService)
    }

    @Bean
    @ConditionalOnMissingBean(WidgetDataSourceResolver::class)
    fun widgetDataSourceResolver(): WidgetDataSourceResolver {
        return WidgetDataSourceResolver()
    }

    @Bean
    @ConditionalOnMissingBean(DashboardDeployer::class)
    fun dashboardDeployer(
        objectMapper: ObjectMapper,
        dashboardRepository: DashboardRepository,
        changelogService: ChangelogService,
        @Value("\${valtimo.changelog.dashboard.clear-tables:false}") clearTables: Boolean
    ): DashboardDeployer {
        return DashboardDeployer(
            objectMapper,
            dashboardRepository,
            changelogService,
            clearTables
        )
    }
}
