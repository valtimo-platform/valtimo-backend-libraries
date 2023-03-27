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

import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.ProcessEngine
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.sql.Date
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

@Transactional
class JobServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var processEngine: ProcessEngine

    @Autowired
    lateinit var jobService: JobService

    @Autowired
    lateinit var camundaProcessService: CamundaProcessService

    @Test
    fun `should delay job`(){
        val testProcessDefinition = "test-timer-event"
        val testProcessInstance = camundaProcessService.startProcess(
            testProcessDefinition, UUID.randomUUID().toString(),null
        )
        processEngine.runtimeService.createMessageCorrelation("message-start-event-offset-delay")
            .processInstanceBusinessKey(testProcessInstance.processInstanceDto.businessKey)
            .correlate()
        val job = processEngine.managementService.createJobQuery().timers().activityId("test-timer").singleResult()
        assertEquals(Date.from(Instant.parse("2150-01-01T00:00:00Z")).toString(),job.duedate.toString())
    }


    @Test
    fun `should move the job forward`(){
        val testProcessDefinition = "test-timer-event"
        val testProcessInstance = camundaProcessService.startProcess(
            testProcessDefinition, UUID.randomUUID().toString(),null
        )
        processEngine.runtimeService.createMessageCorrelation("message-start-event-offset-forward")
            .processInstanceBusinessKey(testProcessInstance.processInstanceDto.businessKey)
            .correlate()
        val job = processEngine.managementService.createJobQuery().timers().activityId("test-timer").singleResult()
        assertEquals(Date.from(Instant.parse("2149-12-31T23:00:00Z")).toString(),job.duedate.toString())
    }

    @Test
    fun `should change job date`(){
        val testProcessDefinition = "test-timer-event"
        val testProcessInstance = camundaProcessService.startProcess(
            testProcessDefinition, UUID.randomUUID().toString(),null
        )
        processEngine.runtimeService.createMessageCorrelation("message-start-event-change-date")
            .processInstanceBusinessKey(testProcessInstance.processInstanceDto.businessKey)
            .correlate()
        val job = processEngine.managementService.createJobQuery().timers().activityId("test-timer").singleResult()
        assertEquals(Date.from(Instant.parse("2300-01-01T00:00:00Z")).toString(),job.duedate.toString())
    }

}