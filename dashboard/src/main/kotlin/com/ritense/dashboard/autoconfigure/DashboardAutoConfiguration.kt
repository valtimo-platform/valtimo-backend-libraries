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

import com.ritense.dashboard.datasource.WidgetDataSourceResolver
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.security.config.DashboardHttpSecurityConfigurer
import com.ritense.dashboard.service.DashboardService
import com.ritense.dashboard.web.rest.AdminDashboardResource
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.dashboard.repository"])
@EntityScan("com.ritense.dashboard.domain")
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
        dashboardRepository: DashboardRepository,
        widgetConfigurationRepository: WidgetConfigurationRepository,
        userManagementService: UserManagementService
    ): DashboardService {
        return DashboardService(dashboardRepository, widgetConfigurationRepository, userManagementService)
    }

    @Bean
    @ConditionalOnMissingBean(AdminDashboardResource::class)
    fun dashboardResource(
        dashboardService: DashboardService
    ): AdminDashboardResource {
        return AdminDashboardResource(dashboardService)
    }

    @Bean
    @ConditionalOnMissingBean(WidgetDataSourceResolver::class)
    fun widgetDataSourceResolver(): WidgetDataSourceResolver {
        return WidgetDataSourceResolver()
    }

}
