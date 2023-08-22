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

package com.ritense.form.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.processlink.dto.FormProcessLinkDeployDto
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.web.rest.dto.FormProcessLinkCreateRequestDto
import com.ritense.form.web.rest.dto.FormProcessLinkResponseDto
import com.ritense.form.web.rest.dto.FormProcessLinkUpdateRequestDto
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import java.util.UUID

class FormProcessLinkMapper(
    objectMapper: ObjectMapper,
    private val formDefinitionService: FormDefinitionService,
) : ProcessLinkMapper {

    init {
        objectMapper.registerSubtypes(
            FormProcessLinkDeployDto::class.java,
            FormProcessLinkResponseDto::class.java,
            FormProcessLinkCreateRequestDto::class.java,
            FormProcessLinkUpdateRequestDto::class.java,
        )
    }

    override fun supportsProcessLinkType(processLinkType: String) = processLinkType == PROCESS_LINK_TYPE_FORM

    override fun toProcessLinkResponseDto(processLink: ProcessLink): ProcessLinkResponseDto {
        processLink as FormProcessLink
        return FormProcessLinkResponseDto(
            id = processLink.id,
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            formDefinitionId = processLink.formDefinitionId
        )
    }

    override fun toProcessLinkCreateRequestDto(deployDto: ProcessLinkDeployDto): ProcessLinkCreateRequestDto {
        deployDto as FormProcessLinkDeployDto

        val formDefinition = formDefinitionService.getFormDefinitionByName(deployDto.formDefinitionName).get()
        return FormProcessLinkCreateRequestDto(
            processDefinitionId = deployDto.processDefinitionId,
            activityId = deployDto.activityId,
            activityType = deployDto.activityType,
            formDefinitionId = formDefinition.id
        )
    }

    override fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink {
        createRequestDto as FormProcessLinkCreateRequestDto
        if (!formDefinitionService.formDefinitionExistsById(createRequestDto.formDefinitionId)) {
            throw RuntimeException("Form definition not found with id ${createRequestDto.formDefinitionId}")
        }
        return FormProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = createRequestDto.processDefinitionId,
            activityId = createRequestDto.activityId,
            activityType = createRequestDto.activityType,
            formDefinitionId = createRequestDto.formDefinitionId
        )
    }

    override fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink {
        updateRequestDto as FormProcessLinkUpdateRequestDto
        assert(processLinkToUpdate.id == updateRequestDto.id)
        if (!formDefinitionService.formDefinitionExistsById(updateRequestDto.formDefinitionId)) {
            throw RuntimeException("Form definition not found with id ${updateRequestDto.formDefinitionId}")
        }
        return FormProcessLink(
            id = updateRequestDto.id,
            processDefinitionId = processLinkToUpdate.processDefinitionId,
            activityId = processLinkToUpdate.activityId,
            activityType = processLinkToUpdate.activityType,
            formDefinitionId = updateRequestDto.formDefinitionId
        )
    }

    companion object {
        const val PROCESS_LINK_TYPE_FORM = "form"
    }
}
