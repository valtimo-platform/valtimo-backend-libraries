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
import com.ritense.logging.service.LoggingEventDeletionService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.beans.factory.annotation.Value
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
        @Value("\${valtimo.logging.retentionInMinutes:60*24*7}") retentionInMinutes: Long,
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


    @Order(HIGHEST_PRECEDENCE + 34)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["loggingLiquibaseMasterChangeLogLocation"])
    fun loggingLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/logging-master.xml")
    }
}