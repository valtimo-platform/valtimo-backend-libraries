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

package com.ritense.gzac

import org.operaton.bpm.engine.impl.cmd.ExecuteJobsCmd
import org.operaton.bpm.engine.impl.context.Context
import org.operaton.bpm.engine.impl.interceptor.Command
import org.operaton.bpm.engine.impl.interceptor.CommandInterceptor
import org.operaton.bpm.engine.impl.persistence.entity.JobEntity
import org.slf4j.MDC


class JobRetryMdcPreInterceptor : CommandInterceptor() {
    override fun <T> execute(command: Command<T?>): T? {
        var mdcSet = false
        if (command is ExecuteJobsCmd) {
            val jobId: String? = (command as ExecuteJobsCmd).jobId
            if (jobId != null) {
                val context = Context.getCommandContext()
                val job: JobEntity? = context
                    .jobManager
                    .findJobById(jobId)
                if (job != null) {
                    MDC.put("operaton.jobId", jobId)
                    MDC.put("operaton.jobRetries", job.getRetries().toString())
                    MDC.put("operaton.jobLastAttempt", (job.getRetries() == 1).toString())
                }
            }
        }
        return next.execute<T?>(command)
    }

    private val ExecuteJobsCmd.jobId: String?
        get() {
            val field = ExecuteJobsCmd::class.java.getDeclaredField("jobId")
            field.isAccessible = true
            return field.get(this) as String?
        }
}