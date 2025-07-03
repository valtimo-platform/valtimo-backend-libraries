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

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.operaton.domain.ProcessInstanceWithDefinition
import com.ritense.valtimo.contract.LoggingConstants
import com.ritense.valtimo.logging.impl.LoggingTestBean
import com.ritense.valtimo.service.OperatonProcessService
import org.operaton.bpm.engine.ManagementService
import org.operaton.bpm.engine.impl.jobexecutor.JobExecutor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.junit5.JUnit5Asserter.fail

class LoggingExecuteJobsRunnableIT @Autowired constructor(
    private val operatonProcessService: OperatonProcessService,
    private val managementService: ManagementService,
    private val jobExecutor: JobExecutor
) : BaseIntegrationTest() {

    private val businessKey = "some-id"

    @Test
    fun `should log correlation id for log messages in job execution`() {
        val processInstanceWithDefinition = runWithoutAuthorization<Any?> {
            operatonProcessService.startProcess(
                "logging-test-process",
                businessKey,
                mapOf()
            )
        } as ProcessInstanceWithDefinition

        var isJobsExecuted = false
        for (i in 0..10) {
            val jobs = managementService
                .createJobQuery()
                .processInstanceId(processInstanceWithDefinition.processInstanceDto.id)
                .list()
            if (jobs.isEmpty()) {
                isJobsExecuted = true
                break
            } else {
                Thread.sleep(100)
            }
        }

        if (!isJobsExecuted) {
            fail("Job was not executed");
        }

        val messageList = LoggingTestBean.listAppender.list
        // MDC should be empty after the job is executed
        Assertions.assertEquals(0, MDC.getCopyOfContextMap().size)
        // in the process 2 messages are logged
        Assertions.assertEquals(2, messageList.size)
        Assertions.assertEquals(
            messageList[0].mdcPropertyMap[LoggingConstants.MDC_CORRELATION_ID_KEY],
            messageList[1].mdcPropertyMap[LoggingConstants.MDC_CORRELATION_ID_KEY]
        )
        // parse as UUID to check if the value is a valid UUID
        UUID.fromString(messageList[0].mdcPropertyMap[LoggingConstants.MDC_CORRELATION_ID_KEY])
    }
}