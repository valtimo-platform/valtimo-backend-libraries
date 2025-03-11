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

package com.ritense.formflow.mapper

import com.ritense.exporter.request.FormFlowDefinitionExportRequest
import com.ritense.form.domain.FormDisplayType
import com.ritense.form.domain.FormSizes
import com.ritense.formflow.domain.FormFlowProcessLink
import com.ritense.formflow.service.FormFlowService
import com.ritense.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import com.ritense.formflow.web.rest.dto.FormFlowProcessLinkExportResponseDto
import com.ritense.formflow.web.rest.dto.FormFlowProcessLinkResponseDto
import com.ritense.formflow.web.rest.dto.FormFlowProcessLinkUpdateRequestDto
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions
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

internal class FormFlowProcessLinkMapperTest {

    @Mock
    lateinit var formFlowService: FormFlowService

    private lateinit var formFlowProcessLinkMapper: FormFlowProcessLinkMapper

    private val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")

    @BeforeEach
    fun beforeEach() {
        MockitoAnnotations.openMocks(this)
        formFlowProcessLinkMapper = FormFlowProcessLinkMapper(
            MapperSingleton.get(),
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
            formFlowDefinitionKey = "formFlowDefinitionKey",
            formDisplayType = FormDisplayType.panel,
            formSize = FormSizes.small,
            subtitles = SUBTITLES
        )

        val formFlowProcessLinkResponseDto = formFlowProcessLinkMapper.toProcessLinkResponseDto(formFlowProcessLink)

        assertTrue(formFlowProcessLinkResponseDto is FormFlowProcessLinkResponseDto)
        assertEquals(formFlowProcessLink.id, formFlowProcessLinkResponseDto.id)
        assertEquals(formFlowProcessLink.processDefinitionId, formFlowProcessLinkResponseDto.processDefinitionId)
        assertEquals(formFlowProcessLink.activityId, formFlowProcessLinkResponseDto.activityId)
        assertEquals(formFlowProcessLink.activityType, formFlowProcessLinkResponseDto.activityType)
        assertEquals(formFlowProcessLink.formFlowDefinitionKey, formFlowProcessLinkResponseDto.formFlowDefinitionKey)
        assertEquals(formFlowProcessLink.subtitles, formFlowProcessLinkResponseDto.subtitles)

    }

    @Test
    fun `should map FormFlowProcessLink entity to export DTO`() {
        val formFlowProcessLink = FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionKey = "formFlowDefinitionKey",
            formDisplayType = FormDisplayType.panel,
            formSize = FormSizes.small,
            subtitles = SUBTITLES
        )

        val dto = formFlowProcessLinkMapper.toProcessLinkExportResponseDto(formFlowProcessLink)

        assertTrue(dto is FormFlowProcessLinkExportResponseDto)
        assertEquals(formFlowProcessLink.activityId, dto.activityId)
        assertEquals(formFlowProcessLink.activityType, dto.activityType)
        assertEquals("formFlowDefinitionKey", dto.formFlowDefinitionKey)
        assertEquals(formFlowProcessLink.formDisplayType, dto.formDisplayType)
        assertEquals(formFlowProcessLink.formSize, dto.formSize)
        assertEquals(formFlowProcessLink.subtitles, dto.subtitles)
    }

    @Test
    fun `should map createRequestDto to FormFlowProcessLink entity`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        val createRequestDto = FormFlowProcessLinkCreateRequestDto(
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionKey = "formFlowDefinitionKey",
            formDisplayType = FormDisplayType.panel,
            formSize = FormSizes.small,
            subtitles = SUBTITLES
        )
        whenever(formFlowService.findDefinition(createRequestDto.formFlowDefinitionKey, caseDefinitionId)).thenReturn(mock())

        val formFlowProcessLink = formFlowProcessLinkMapper.toNewProcessLink(createRequestDto, caseDefinitionId)

        assertTrue(formFlowProcessLink is FormFlowProcessLink)
        assertEquals(createRequestDto.processDefinitionId, formFlowProcessLink.processDefinitionId)
        assertEquals(createRequestDto.activityId, formFlowProcessLink.activityId)
        assertEquals(createRequestDto.activityType, formFlowProcessLink.activityType)
        assertEquals(createRequestDto.formFlowDefinitionKey, formFlowProcessLink.formFlowDefinitionKey)
        assertEquals(createRequestDto.formDisplayType, formFlowProcessLink.formDisplayType)
        assertEquals(createRequestDto.formSize, formFlowProcessLink.formSize)
        assertEquals(createRequestDto.subtitles, formFlowProcessLink.subtitles)
    }

    @Test
    fun `should map updateRequestDto to FormFlowProcessLink entity`() {
        val processLinkToUpdate = FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionKey = "formFlowDefinitionKeyOld"
        )
        val updateRequestDto = FormFlowProcessLinkUpdateRequestDto(
            id = processLinkToUpdate.id,
            formFlowDefinitionKey = "formFlowDefinitionKey",
            formDisplayType = FormDisplayType.panel,
            formSize = FormSizes.small,
            subtitles = SUBTITLES
        )
        whenever(formFlowService.findDefinition(updateRequestDto.formFlowDefinitionKey, caseDefinitionId)).thenReturn(mock())

        val formFlowProcessLink = formFlowProcessLinkMapper.toUpdatedProcessLink(processLinkToUpdate, updateRequestDto, caseDefinitionId)

        assertTrue(formFlowProcessLink is FormFlowProcessLink)
        assertEquals(processLinkToUpdate.processDefinitionId, formFlowProcessLink.processDefinitionId)
        assertEquals(processLinkToUpdate.activityId, formFlowProcessLink.activityId)
        assertEquals(processLinkToUpdate.activityType, formFlowProcessLink.activityType)
        assertEquals(updateRequestDto.formFlowDefinitionKey, formFlowProcessLink.formFlowDefinitionKey)
        assertEquals(updateRequestDto.formDisplayType, formFlowProcessLink.formDisplayType)
        assertEquals(updateRequestDto.formSize, formFlowProcessLink.formSize)
        assertEquals(updateRequestDto.subtitles, formFlowProcessLink.subtitles)
    }

    @Test
    fun `should throw error when formFlowDefinition doesn't exist in toNewProcessLink`() {
        val createRequestDto = FormFlowProcessLinkCreateRequestDto(
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionKey = "formFlowDefinitionKey"
        )

        val exception = assertThrows<RuntimeException> {
            formFlowProcessLinkMapper.toNewProcessLink(createRequestDto, caseDefinitionId)
        }

        assertEquals(
            "FormFlow definition not found with id ${createRequestDto.formFlowDefinitionKey}",
            exception.message
        )
    }

    @Test
    fun `should throw error when formFlowDefinition doesn't exist in toUpdatedProcessLink`() {
        val processLinkToUpdate = FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionKey = "formFlowDefinitionKeyOld"
        )
        val updateRequestDto = FormFlowProcessLinkUpdateRequestDto(
            id = processLinkToUpdate.id,
            formFlowDefinitionKey = "formFlowDefinitionKey"
        )

        val exception = assertThrows<RuntimeException> {
            formFlowProcessLinkMapper.toUpdatedProcessLink(processLinkToUpdate, updateRequestDto, caseDefinitionId)
        }

        assertEquals(
            "FormFlow definition not found with id ${updateRequestDto.formFlowDefinitionKey}",
            exception.message
        )
    }

    @Test
    fun `should return related export request for form-flow process links`() {
        val formProcessLink = FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "processDefinitionId",
            activityId = "activityId",
            activityType = SERVICE_TASK_START,
            formFlowDefinitionKey = "testing",
        )

        val relatedExportRequests = formFlowProcessLinkMapper.createRelatedExportRequests(formProcessLink, caseDefinitionId)

        Assertions.assertThat(relatedExportRequests).contains(
            FormFlowDefinitionExportRequest("testing", caseDefinitionId)
        )
    }

    companion object {
        val SUBTITLES = listOf("test", "test2")
    }
}
