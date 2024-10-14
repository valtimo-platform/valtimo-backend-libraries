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

package com.ritense.logging

import com.ritense.logging.domain.LoggingEvent
import com.ritense.logging.domain.LoggingEventException
import com.ritense.logging.domain.LoggingEventProperty
import com.ritense.logging.repository.LoggingEventExceptionRepository
import com.ritense.logging.repository.LoggingEventPropertyRepository
import com.ritense.logging.repository.LoggingEventRepository
import com.ritense.logging.security.config.LoggingHttpSecurityConfigurer
import com.ritense.logging.service.LoggingEventDeletionService
import com.ritense.logging.service.LoggingEventService
import com.ritense.logging.web.rest.LoggingEventManagementResource
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.contract.hardening.service.HardeningService
import com.ritense.valtimo.contract.web.rest.error.ExceptionTranslator
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.util.ErrorHandler
import javax.sql.DataSource

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        LoggingEventRepository::class,
        LoggingEventPropertyRepository::class,
        LoggingEventExceptionRepository::class,
    ]
)
@EntityScan(
    basePackageClasses = [
        LoggingEvent::class,
        LoggingEventProperty::class,
        LoggingEventException::class,
    ]
)
class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LoggableResourceAspect::class)
    fun loggableResourceAspect(): LoggableResourceAspect {
        return LoggableResourceAspect()
    }

    @Bean
    @ConditionalOnMissingBean(LoggingEventDeletionService::class)
    fun loggingEventDeletionService(
        @Value("\${valtimo.logging.retentionInMinutes:30240}") retentionInMinutes: Long,
        loggingEventRepository: LoggingEventRepository,
        loggingEventPropertyRepository: LoggingEventPropertyRepository,
        loggingEventExceptionRepository: LoggingEventExceptionRepository
    ): LoggingEventDeletionService {
        return LoggingEventDeletionService(
            retentionInMinutes,
            loggingEventRepository,
            loggingEventPropertyRepository,
            loggingEventExceptionRepository,
        )
    }

    @Bean
    @ConditionalOnMissingBean(LoggingEventService::class)
    fun loggingEventService(
        loggingEventRepository: LoggingEventRepository,
    ): LoggingEventService {
        return LoggingEventService(
            loggingEventRepository,
        )
    }

    @Bean
    @ConditionalOnMissingBean(LoggingEventManagementResource::class)
    fun loggingEventManagementResource(
        loggingEventService: LoggingEventService,
    ): LoggingEventManagementResource {
        return LoggingEventManagementResource(
            loggingEventService,
        )
    }

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(LoggingHttpSecurityConfigurer::class)
    fun loggingHttpSecurityConfigurer(): LoggingHttpSecurityConfigurer {
        return LoggingHttpSecurityConfigurer()
    }

    @Order(HIGHEST_PRECEDENCE + 34)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["loggingLiquibaseMasterChangeLogLocation"])
    fun loggingLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/logging-master.xml")
    }

    @Order(LOWEST_PRECEDENCE - 100)
    @Bean
    fun loggingContextExceptionTranslator(
        hardeningService: HardeningService?
    ): ExceptionTranslator {
        return LoggingContextExceptionTranslator(hardeningService)
    }

    @ConditionalOnMissingBean(LoggingErrorWithContextErrorHandler::class)
    @Bean
    fun loggingErrorWithContextErrorHandler(): ErrorHandler {
        return LoggingErrorWithContextErrorHandler()
    }

    @ConditionalOnBean(ThreadPoolTaskScheduler::class)
    @ConditionalOnMissingBean(name = ["loggingErrorWithContextThreadPoolTaskScheduler"])
    @Bean
    fun loggingErrorWithContextThreadPoolTaskScheduler(
        threadPoolTaskScheduler: ThreadPoolTaskScheduler,
        loggingErrorWithContextErrorHandler: ErrorHandler,
    ): ThreadPoolTaskScheduler {
        threadPoolTaskScheduler.setErrorHandler(loggingErrorWithContextErrorHandler)
        return threadPoolTaskScheduler
    }
}