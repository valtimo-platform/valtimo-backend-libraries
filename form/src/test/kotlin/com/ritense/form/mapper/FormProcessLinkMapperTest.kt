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

package com.ritense.form.mapper

import com.ritense.exporter.request.FormDefinitionExportRequest
import com.ritense.form.domain.FormDisplayType
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.FormSizes
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.web.rest.dto.FormProcessLinkCreateRequestDto
import com.ritense.form.web.rest.dto.FormProcessLinkResponseDto
import com.ritense.form.web.rest.dto.FormProcessLinkUpdateRequestDto
import com.ritense.processlink.domain.ActivityTypeWithEventName.USER_TASK_CREATE
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FormProcessLinkMapperTest {

    @Mock
    lateinit var formDefinitionService: FormDefinitionService

    private lateinit var formProcessLinkMapper: FormProcessLinkMapper

    @BeforeEach
    fun beforeEach() {
        MockitoAnnotations.openMocks(this)
        formProcessLinkMapper = FormProcessLinkMapper(
            MapperSingleton.get(),
            formDefinitionService,
        )
    }

    @Test
    fun `should map FormProcessLink entity to dto`() {
        val formProcessLink = FormProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = USER_TASK_CREATE,
            formDefinitionId = UUID.randomUUID(),
            viewModelEnabled = false,
            formDisplayType = FormDisplayType.panel,
            formSize = FormSizes.small,
            subtitles = SUBTITLES,
        )

        val formProcessLinkResponseDto = formProcessLinkMapper.toProcessLinkResponseDto(formProcessLink)

        assertTrue(formProcessLinkResponseDto is FormProcessLinkResponseDto)
        assertEquals(formProcessLink.id, formProcessLinkResponseDto.id)
        assertEquals(formProcessLink.processDefinitionId, formProcessLinkResponseDto.processDefinitionId)
        assertEquals(formProcessLink.activityId, formProcessLinkResponseDto.activityId)
        assertEquals(formProcessLink.activityType, formProcessLinkResponseDto.activityType)
        assertEquals(formProcessLink.formDefinitionId, formProcessLinkResponseDto.formDefinitionId)
        assertEquals(formProcessLink.viewModelEnabled, formProcessLinkResponseDto.viewModelEnabled)
        assertEquals(formProcessLink.formDisplayType, formProcessLinkResponseDto.formDisplayType)
        assertEquals(formProcessLink.formSize, formProcessLinkResponseDto.formSize)
        assertEquals(formProcessLink.subtitles, formProcessLinkResponseDto.subtitles)
    }

    @Test
    fun `should map createRequestDto to FormProcessLink entity`() {
        val createRequestDto = FormProcessLinkCreateRequestDto(
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = USER_TASK_CREATE,
            formDefinitionId = UUID.randomUUID(),
            viewModelEnabled = false,
            formDisplayType = FormDisplayType.panel,
            formSize = FormSizes.small,
            subtitles = SUBTITLES,
        )
        whenever(formDefinitionService.formDefinitionExistsById(createRequestDto.formDefinitionId)).thenReturn(true)

        val formProcessLink = formProcessLinkMapper.toNewProcessLink(createRequestDto)

        assertTrue(formProcessLink is FormProcessLink)
        assertEquals(createRequestDto.processDefinitionId, formProcessLink.processDefinitionId)
        assertEquals(createRequestDto.activityId, formProcessLink.activityId)
        assertEquals(createRequestDto.activityType, formProcessLink.activityType)
        assertEquals(createRequestDto.formDefinitionId, formProcessLink.formDefinitionId)
        assertEquals(createRequestDto.viewModelEnabled, formProcessLink.viewModelEnabled)
        assertEquals(createRequestDto.formDisplayType, formProcessLink.formDisplayType)
        assertEquals(createRequestDto.formSize, formProcessLink.formSize)
        assertEquals(createRequestDto.subtitles, formProcessLink.subtitles)
    }

    @Test
    fun `should map updateRequestDto to FormProcessLink entity`() {
        val processLinkToUpdate = FormProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = USER_TASK_CREATE,
            formDefinitionId = UUID.randomUUID(),
            viewModelEnabled = false,
        )
        val updateRequestDto = FormProcessLinkUpdateRequestDto(
            id = processLinkToUpdate.id,
            formDefinitionId = UUID.randomUUID(),
            viewModelEnabled = false,
            formDisplayType = FormDisplayType.panel,
            formSize = FormSizes.small,
            subtitles = SUBTITLES
        )
        whenever(formDefinitionService.formDefinitionExistsById(updateRequestDto.formDefinitionId)).thenReturn(true)

        val formProcessLink = formProcessLinkMapper.toUpdatedProcessLink(processLinkToUpdate, updateRequestDto)

        assertTrue(formProcessLink is FormProcessLink)
        assertEquals(processLinkToUpdate.processDefinitionId, formProcessLink.processDefinitionId)
        assertEquals(processLinkToUpdate.activityId, formProcessLink.activityId)
        assertEquals(processLinkToUpdate.activityType, formProcessLink.activityType)
        assertEquals(updateRequestDto.formDefinitionId, formProcessLink.formDefinitionId)
        assertEquals(updateRequestDto.viewModelEnabled, formProcessLink.viewModelEnabled)
        assertEquals(updateRequestDto.formDisplayType, formProcessLink.formDisplayType)
        assertEquals(updateRequestDto.formSize, formProcessLink.formSize)
        assertEquals(updateRequestDto.subtitles, formProcessLink.subtitles)
    }

    @Test
    fun `should throw error when formDefinition doesn't exist in toNewProcessLink`() {
        val createRequestDto = FormProcessLinkCreateRequestDto(
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = USER_TASK_CREATE,
            formDefinitionId = UUID.randomUUID(),
            viewModelEnabled = false
        )

        val exception = assertThrows<RuntimeException> {
            formProcessLinkMapper.toNewProcessLink(createRequestDto)
        }

        assertEquals("Form definition not found with id ${createRequestDto.formDefinitionId}", exception.message)
    }

    @Test
    fun `should throw error when formDefinition doesn't exist in toUpdatedProcessLink`() {
        val processLinkToUpdate = FormProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = USER_TASK_CREATE,
            formDefinitionId = UUID.randomUUID(),
            viewModelEnabled = false
        )
        val updateRequestDto = FormProcessLinkUpdateRequestDto(
            id = processLinkToUpdate.id,
            formDefinitionId = UUID.randomUUID(),
            viewModelEnabled = false
        )

        val exception = assertThrows<RuntimeException> {
            formProcessLinkMapper.toUpdatedProcessLink(processLinkToUpdate, updateRequestDto)
        }

        assertEquals("Form definition not found with id ${updateRequestDto.formDefinitionId}", exception.message)
    }

    @Test
    fun `should return related export request for form process links`() {
        val formDefinition = FormIoFormDefinition(
            UUID.randomUUID(),
            "testing",
            "{}",
            true
        )
        val formProcessLink = FormProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = USER_TASK_CREATE,
            formDefinitionId = formDefinition.id,
            viewModelEnabled = false
        )

        whenever(formDefinitionService.getFormDefinitionById(formProcessLink.formDefinitionId))
            .thenReturn(Optional.of(formDefinition))
        val relatedExportRequests = formProcessLinkMapper.createRelatedExportRequests(formProcessLink)

        assertThat(relatedExportRequests).contains(
            FormDefinitionExportRequest("testing")
        )
    }

    companion object {
        val SUBTITLES = listOf("test", "test2")
    }
}
