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

package com.ritense.valtimo.formflow.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formflow.service.FormFlowService
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.valtimo.formflow.domain.FormFlowProcessLink
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkResponseDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkUpdateRequestDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FormFlowFlowProcessLinkMapperTest {

    @Mock
    lateinit var formFlowService: FormFlowService

    private lateinit var formFlowProcessLinkMapper: FormFlowProcessLinkMapper

    @BeforeEach
    fun beforeEach() {
        MockitoAnnotations.openMocks(this)
        formFlowProcessLinkMapper = FormFlowProcessLinkMapper(
            jacksonObjectMapper(),
            formFlowService,
        )
    }

    @Test
    fun `should map FormFlowProcessLink entity to dto`() {
        val formFlowProcessLink = FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionId = "formFlowDefinitionId:latest"
        )

        val formFlowProcessLinkResponseDto = formFlowProcessLinkMapper.toProcessLinkResponseDto(formFlowProcessLink)

        assertTrue(formFlowProcessLinkResponseDto is FormFlowProcessLinkResponseDto)
        assertEquals(formFlowProcessLink.id, formFlowProcessLinkResponseDto.id)
        assertEquals(formFlowProcessLink.processDefinitionId, formFlowProcessLinkResponseDto.processDefinitionId)
        assertEquals(formFlowProcessLink.activityId, formFlowProcessLinkResponseDto.activityId)
        assertEquals(formFlowProcessLink.activityType, formFlowProcessLinkResponseDto.activityType)
        assertEquals(formFlowProcessLink.formFlowDefinitionId, formFlowProcessLinkResponseDto.formFlowDefinitionId)
    }

    @Test
    fun `should map createRequestDto to FormFlowProcessLink entity`() {
        val createRequestDto = FormFlowProcessLinkCreateRequestDto(
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionId = "formFlowDefinitionId:latest"
        )
        whenever(formFlowService.findDefinition(createRequestDto.formFlowDefinitionId)).thenReturn(mock())

        val formFlowProcessLink = formFlowProcessLinkMapper.toNewProcessLink(createRequestDto)

        assertTrue(formFlowProcessLink is FormFlowProcessLink)
        assertEquals(createRequestDto.processDefinitionId, formFlowProcessLink.processDefinitionId)
        assertEquals(createRequestDto.activityId, formFlowProcessLink.activityId)
        assertEquals(createRequestDto.activityType, formFlowProcessLink.activityType)
        assertEquals(createRequestDto.formFlowDefinitionId, formFlowProcessLink.formFlowDefinitionId)
    }

    @Test
    fun `should map updateRequestDto to FormFlowProcessLink entity`() {
        val processLinkToUpdate = FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionId = "formFlowDefinitionId:1"
        )
        val updateRequestDto = FormFlowProcessLinkUpdateRequestDto(
            id = processLinkToUpdate.id,
            formFlowDefinitionId = "formFlowDefinitionId:latest"
        )
        whenever(formFlowService.findDefinition(updateRequestDto.formFlowDefinitionId)).thenReturn(mock())

        val formFlowProcessLink = formFlowProcessLinkMapper.toUpdatedProcessLink(processLinkToUpdate, updateRequestDto)

        assertTrue(formFlowProcessLink is FormFlowProcessLink)
        assertEquals(processLinkToUpdate.processDefinitionId, formFlowProcessLink.processDefinitionId)
        assertEquals(processLinkToUpdate.activityId, formFlowProcessLink.activityId)
        assertEquals(processLinkToUpdate.activityType, formFlowProcessLink.activityType)
        assertEquals(updateRequestDto.formFlowDefinitionId, formFlowProcessLink.formFlowDefinitionId)
    }

    @Test
    fun `should throw error when formFlowDefinition doesn't exist in toNewProcessLink`() {
        val createRequestDto = FormFlowProcessLinkCreateRequestDto(
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionId = "formFlowDefinitionId:latest"
        )

        val exception = assertThrows<RuntimeException> {
            formFlowProcessLinkMapper.toNewProcessLink(createRequestDto)
        }

        assertEquals("FormFlow definition not found with id ${createRequestDto.formFlowDefinitionId}", exception.message)
    }

    @Test
    fun `should throw error when formFlowDefinition doesn't exist in toUpdatedProcessLink`() {
        val processLinkToUpdate = FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionId = "formFlowDefinitionId:latest"
        )
        val updateRequestDto = FormFlowProcessLinkUpdateRequestDto(
            id = processLinkToUpdate.id,
            formFlowDefinitionId = "formFlowDefinitionId:latest"
        )

        val exception = assertThrows<RuntimeException> {
            formFlowProcessLinkMapper.toUpdatedProcessLink(processLinkToUpdate, updateRequestDto)
        }

        assertEquals("FormFlow definition not found with id ${updateRequestDto.formFlowDefinitionId}", exception.message)
    }
}
