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

package com.ritense.valtimo.logging

import com.ritense.valtimo.contract.LoggingConstants
import org.operaton.bpm.engine.impl.ProcessEngineImpl
import org.operaton.bpm.engine.impl.interceptor.CommandExecutor
import org.operaton.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable
import org.operaton.bpm.engine.impl.jobexecutor.JobFailureCollector
import org.slf4j.MDC
import java.util.UUID

class LoggingExecuteJobsRunnable(
    jobIds: List<String>,
    processEngine: ProcessEngineImpl
):ExecuteJobsRunnable(
    jobIds,
    processEngine
) {
    override fun executeJob(
        nextJobId: String?,
        commandExecutor: CommandExecutor?,
        jobFailureCollector: JobFailureCollector?
    ) {
        try {
            MDC.put(LoggingConstants.MDC_CORRELATION_ID_KEY, UUID.randomUUID().toString())
            super.executeJob(nextJobId, commandExecutor, jobFailureCollector)
        } finally {
            MDC.remove(LoggingConstants.MDC_CORRELATION_ID_KEY)
        }
    }
}