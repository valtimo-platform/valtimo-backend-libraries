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

package com.ritense.logging.service

import com.ritense.logging.repository.LoggingEventExceptionRepository
import com.ritense.logging.repository.LoggingEventExceptionSpecificationHelper
import com.ritense.logging.repository.LoggingEventPropertyRepository
import com.ritense.logging.repository.LoggingEventPropertySpecificationHelper
import com.ritense.logging.repository.LoggingEventRepository
import com.ritense.logging.repository.LoggingEventSpecificationHelper
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Transactional
@Service
@SkipComponentScan
class LoggingEventDeletionService(
    private val retentionInMinutes: Long,
    private val loggingEventRepository: LoggingEventRepository,
    private val loggingEventPropertyRepository: LoggingEventPropertyRepository,
    private val loggingEventExceptionRepository: LoggingEventExceptionRepository,
) {

    @Scheduled(
        cron = "\${valtimo.logging.deletionCron:0 0 4 * * ?}"
    )
    fun deleteOldLoggingEvents() {
        val retentionDateTime = LocalDateTime.now() - Duration.ofMinutes(retentionInMinutes)
        loggingEventPropertyRepository.delete(LoggingEventPropertySpecificationHelper.byOlderThan(retentionDateTime))
        loggingEventExceptionRepository.delete(LoggingEventExceptionSpecificationHelper.byOlderThan(retentionDateTime))
        loggingEventRepository.delete(LoggingEventSpecificationHelper.byOlderThan(retentionDateTime))
    }
}
