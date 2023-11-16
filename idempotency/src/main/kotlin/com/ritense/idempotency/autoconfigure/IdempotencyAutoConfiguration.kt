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

package com.ritense.idempotency.autoconfigure

import com.ritense.idempotency.repository.IdempotencyMessageRepository
import com.ritense.idempotency.service.IdempotencyMessageDeletionService
import com.ritense.idempotency.service.IdempotencyMessageService
import javax.sql.DataSource
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableJpaRepositories(
    basePackageClasses = [
        IdempotencyMessageRepository::class
    ]
)
@EntityScan(basePackages = ["com.ritense.idempotency"])
@AutoConfigureAfter(DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class)
@EnableConfigurationProperties(LiquibaseProperties::class)
class IdempotencyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotencyLiquibaseRunner::class)
    fun idempotencyLiquibaseRunner(
        liquibaseProperties: LiquibaseProperties,
        datasource: DataSource
    ) = IdempotencyLiquibaseRunner(
        liquibaseProperties,
        datasource
    )

    @Bean
    fun idempotencyMessageService(
        idempotencyMessageRepository: IdempotencyMessageRepository
    ) = IdempotencyMessageService(
        idempotencyMessageRepository
    )

    @Bean
    fun idempotencyMessageDeletionService(
        idempotencyMessageRepository: IdempotencyMessageRepository
    ) = IdempotencyMessageDeletionService(
        idempotencyMessageRepository
    )
}