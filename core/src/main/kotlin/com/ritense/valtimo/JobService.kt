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

package com.ritense.valtimo

import java.time.ZonedDateTime
import java.util.Date
import mu.KotlinLogging
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Job

class JobService(val processEngine: ProcessEngine) {

    fun addOffsetInMillisToTimerDueDateByActivityId(
        millisecondsToAdd: Long, activityId: String, execution: DelegateExecution
    ) {

        getJobByActivityIdAndProcessInstanceId(activityId, execution.processInstanceId).apply {
            val dueDate = Date.from(
                this.duedate.toInstant().plusMillis(millisecondsToAdd)
            )
            processEngine.managementService.setJobDuedate(this.id, dueDate).also {
                logger.debug {
                    "Changing the date of timer ${this.id} from ${this.duedate} to $dueDate " +
                        "for process instance $processInstanceId"
                }
            }
        }
    }

    fun updateTimerDueDateByActivityId(dueDateString: String, activityId: String, execution: DelegateExecution) {
        val dueDate = Date.from(ZonedDateTime.parse(dueDateString).toInstant())

        getJobByActivityIdAndProcessInstanceId(activityId, execution.processInstanceId).apply {
            processEngine.managementService.setJobDuedate(this.id, dueDate).also {
                logger.debug {
                    "Changing the date of timer ${this.id} from ${this.duedate} to $dueDate " +
                        "for process instance $processInstanceId"
                }
            }
        }
    }

    private fun getJobByActivityIdAndProcessInstanceId(
        jobActivityId: String,
        processInstanceId: String,
    ): Job {

        return processEngine.managementService.createJobQuery().timers().processInstanceId(processInstanceId)
            .activityId(jobActivityId).singleResult() ?: throw ProcessEngineException(
            "No job with $jobActivityId found for process with Id $processInstanceId"
        )
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }

}