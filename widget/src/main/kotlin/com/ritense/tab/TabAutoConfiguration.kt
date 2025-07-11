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

package com.ritense.tab

import com.ritense.tab.domain.Tab
import com.ritense.tab.repository.TabRepository
import com.ritense.tab.security.TabHttpSecurityConfigurer
import com.ritense.tab.service.TabService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        TabRepository::class,
    ]
)
@EntityScan(
    basePackageClasses = [
        Tab::class,
    ]
)
class TabAutoConfiguration {


    @Order(270)
    @Bean
    @ConditionalOnMissingBean(TabHttpSecurityConfigurer::class)
    fun tabHttpSecurityConfigurer(): TabHttpSecurityConfigurer {
        return TabHttpSecurityConfigurer()
    }

    @Order(HIGHEST_PRECEDENCE + 34)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["widgetLiquibaseMasterChangeLogLocation"])
    fun widgetLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/widget-master.xml")
    }


    @Bean
    @ConditionalOnClass(DataSource::class)
    fun tabService(
        tabRepository: TabRepository,
    ): TabService {
        return TabService(
            tabRepository,
        )
    }

}