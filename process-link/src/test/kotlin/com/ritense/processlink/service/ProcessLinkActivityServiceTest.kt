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

package com.ritense.processlink.service

import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID


class ProcessLinkActivityServiceTest {

    @Mock
    lateinit var taskService: CamundaTaskService

    @Mock
    lateinit var processLinkService: ProcessLinkService

    @Mock
    lateinit var processLinkActivityHandler: ProcessLinkActivityHandler<Map<String, Any>>

    lateinit var processLinkActivityService: ProcessLinkActivityService

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
        processLinkActivityService = ProcessLinkActivityService(processLinkService, taskService, listOf(processLinkActivityHandler))
    }

    @Test
    fun `should use task provider to open task`() {
        val taskId = UUID.randomUUID()
        val task: CamundaTask = mock()
        whenever(task.id).thenReturn(taskId.toString())
        whenever(task.getProcessDefinitionId()).thenReturn("some-process:1")
        whenever(task.taskDefinitionKey).thenReturn("some-activity")

        val processLink: ProcessLink = mock()
        val processLinkActivityResult = ProcessLinkActivityResult<Map<String,Any>>(UUID.randomUUID(), "test", mapOf())

        whenever(taskService.findTask(any())).thenReturn(task)
        whenever(processLinkService.getProcessLinks(any(), any())).thenReturn(listOf(processLink))
        whenever(processLinkActivityHandler.supports(processLink)).thenReturn(true)
        whenever(processLinkActivityHandler.openTask(task, processLink)).thenReturn(processLinkActivityResult)

        processLinkActivityService.openTask(taskId)

        verify(processLinkActivityHandler).supports(processLink)
        verify(processLinkActivityHandler).openTask(task, processLink)
    }
}