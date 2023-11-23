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

package com.ritense.outbox.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ritense.outbox.publisher.DefaultMessagePublisher
import com.ritense.outbox.publisher.MessagePublisher
import com.ritense.outbox.publisher.PollingPublisherJob
import com.ritense.outbox.publisher.PollingPublisherService
import com.ritense.outbox.repository.OutboxMessageRepository
import com.ritense.outbox.service.CloudEventOutboxService
import com.ritense.outbox.service.DefaultOutboxService
import com.ritense.outbox.service.DomainEventOutboxService
import com.ritense.outbox.service.OutboxService
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
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        OutboxMessageRepository::class
    ]
)
@EntityScan(basePackages = ["com.ritense.outbox"])
@AutoConfigureAfter(DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class)
@EnableConfigurationProperties(LiquibaseProperties::class)
class OutboxAutoConfiguration {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .findAndRegisterModules()
        .registerModule(KotlinModule.Builder().build())

    @Bean
    @ConditionalOnMissingBean(OutboxLiquibaseRunner::class)
    fun outboxLiquibaseRunner(
        liquibaseProperties: LiquibaseProperties,
        datasource: DataSource
    ): OutboxLiquibaseRunner {
        return OutboxLiquibaseRunner(liquibaseProperties, datasource)
    }

    @Bean
    @ConditionalOnMissingBean(DefaultOutboxService::class)
    fun defaultOutboxService(
        outboxMessageRepository: OutboxMessageRepository,
        objectMapper: ObjectMapper,
    ): DefaultOutboxService {
        return DefaultOutboxService(
            outboxMessageRepository,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnMissingBean(CloudEventOutboxService::class)
    fun cloudEventOutboxService(
        defaultOutboxService: DefaultOutboxService
    ): CloudEventOutboxService {
        return CloudEventOutboxService(
            defaultOutboxService
        )
    }

    @Bean
    @ConditionalOnMissingBean(DomainEventOutboxService::class)
    fun domainEventOutboxService(
        cloudEventOutboxService: CloudEventOutboxService,
        objectMapper: ObjectMapper
    ): DomainEventOutboxService {
        return DomainEventOutboxService(
            cloudEventOutboxService,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnMissingBean(PollingPublisherService::class)
    fun pollingPublisherService(
        defaultOutboxService: DefaultOutboxService,
        messagePublisher: MessagePublisher,
        platformTransactionManager: PlatformTransactionManager
    ): PollingPublisherService {
        return PollingPublisherService(
            defaultOutboxService,
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
    @ConditionalOnMissingBean(MessagePublisher::class)
    fun defaultMessagePublisher() = DefaultMessagePublisher()

}