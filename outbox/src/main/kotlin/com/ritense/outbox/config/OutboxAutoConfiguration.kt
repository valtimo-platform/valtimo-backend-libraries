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

package com.ritense.outbox.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.OutboxLiquibaseRunner
import com.ritense.outbox.OutboxMessage
import com.ritense.outbox.repository.OutboxMessageRepository
import com.ritense.outbox.OutboxService
import com.ritense.outbox.UserProvider
import com.ritense.outbox.ValtimoOutboxService
import com.ritense.outbox.config.condition.ConditionalOnOutboxEnabled
import com.ritense.outbox.repository.impl.MySqlOutboxMessageRepository
import com.ritense.outbox.repository.impl.PostgresOutboxMessageRepository
import com.ritense.outbox.publisher.MessagePublisher
import com.ritense.outbox.publisher.PollingPublisherJob
import com.ritense.outbox.publisher.PollingPublisherService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import java.util.UUID
import javax.sql.DataSource

@AutoConfiguration
@ConditionalOnOutboxEnabled
@EnableJpaRepositories(basePackages = ["com.ritense.outbox.repository.impl"])
@EntityScan(basePackages = ["com.ritense.outbox"])
@AutoConfigureAfter(DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class)
@EnableConfigurationProperties(LiquibaseProperties::class)
class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OutboxLiquibaseRunner::class)
    fun outboxLiquibaseRunner(
        liquibaseProperties: LiquibaseProperties,
        datasource: DataSource
    ): OutboxLiquibaseRunner {
        return OutboxLiquibaseRunner(liquibaseProperties, datasource)
    }

    @ConditionalOnMissingBean(UserProvider::class)
    @Bean
    fun userProvider(
    ): UserProvider {
        return UserProvider()
    }

    @Bean
    @ConditionalOnMissingBean(OutboxService::class)
    fun defaultOutboxService(
        outboxMessageRepository: OutboxMessageRepository,
        objectMapper: ObjectMapper,
        userProvider: UserProvider,
        @Value("\${valtimo.outbox.publisher.cloudevent-source:\${spring.application.name:application}}") cloudEventSource: String,
    ): OutboxService {
        return ValtimoOutboxService(
            outboxMessageRepository,
            objectMapper,
            userProvider,
            cloudEventSource,
        )
    }

    @Bean
    @ConditionalOnMissingBean(PollingPublisherService::class)
    fun pollingPublisherService(
        outboxService: ValtimoOutboxService,
        messagePublisher: MessagePublisher,
        platformTransactionManager: PlatformTransactionManager
    ): PollingPublisherService {
        return PollingPublisherService(
            outboxService,
            messagePublisher,
            platformTransactionManager
        )
    }

    @Bean
    @ConditionalOnMissingBean(PollingPublisherJob::class)
    fun pollingPublisherJob(
        pollingPublisherService: PollingPublisherService
    ): PollingPublisherJob {
        return PollingPublisherJob(pollingPublisherService)
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = ["database"], havingValue = "postgres")
    fun postgresOutboxMessageRepository(): JpaRepositoryFactoryBean<OutboxMessageRepository, OutboxMessage, UUID> {
        return JpaRepositoryFactoryBean(PostgresOutboxMessageRepository::class.java)
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = ["database"], havingValue = "mysql", matchIfMissing = true)
    fun mySqlOutboxMessageRepository(): JpaRepositoryFactoryBean<OutboxMessageRepository, OutboxMessage, UUID> {
        return JpaRepositoryFactoryBean(MySqlOutboxMessageRepository::class.java)
    }
}
