/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.valtimo.changelog.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@EnableJpaRepositories(basePackageClasses = [ChangesetRepository::class])
@EntityScan("com.ritense.valtimo.changelog.domain")
@Configuration
class ChangelogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChangelogService::class)
    fun changelogService(
        resourceLoader: ResourceLoader,
        changesetRepository: ChangesetRepository,
        objectMapper: ObjectMapper,
    ): ChangelogService {
        return ChangelogService(
            resourceLoader,
            changesetRepository,
            objectMapper,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ChangelogDeployer::class)
    fun changelogDeployer(
       changelogService: ChangelogService,
       changesetDeployers: List<ChangesetDeployer>,
    ): ChangelogDeployer {
        return ChangelogDeployer(
            changelogService,
            changesetDeployers
        )
    }

    @ConditionalOnClass(DataSource::class)
    @Order(HIGHEST_PRECEDENCE + 30)
    @Bean
    @ConditionalOnMissingBean(name = ["changelogLiquibaseMasterChangeLogLocation"])
    fun changelogLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/changelog-master.xml")
    }

}