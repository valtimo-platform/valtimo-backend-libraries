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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.service.FormFlowService
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.valtimo.formflow.domain.FormFlowProcessLink
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkResponseDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkUpdateRequestDto
import java.util.UUID

class FormFlowProcessLinkMapper(
    objectMapper: ObjectMapper,
    private val formFlowService: FormFlowService,
) : ProcessLinkMapper {

    init {
        objectMapper.registerSubtypes(
            FormFlowProcessLinkResponseDto::class.java,
            FormFlowProcessLinkCreateRequestDto::class.java,
            FormFlowProcessLinkUpdateRequestDto::class.java,
        )
    }

    override fun supportsProcessLinkType(processLinkType: String) = processLinkType == PROCESS_LINK_TYPE_FORM_FLOW

    override fun toProcessLinkResponseDto(processLink: ProcessLink): ProcessLinkResponseDto {
        processLink as FormFlowProcessLink
        return FormFlowProcessLinkResponseDto(
            id = processLink.id,
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            activityType = processLink.activityType,
            formFlowDefinitionId = processLink.formFlowDefinitionId
        )
    }

    override fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink {
        createRequestDto as FormFlowProcessLinkCreateRequestDto
        if (formFlowService.findDefinition(createRequestDto.formFlowDefinitionId) == null) {
            throw RuntimeException("FormFlow definition not found with id ${createRequestDto.formFlowDefinitionId}")
        }
        return FormFlowProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = createRequestDto.processDefinitionId,
            activityId = createRequestDto.activityId,
            activityType = createRequestDto.activityType,
            formFlowDefinitionId = createRequestDto.formFlowDefinitionId
        )
    }

    override fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink {
        updateRequestDto as FormFlowProcessLinkUpdateRequestDto
        if (formFlowService.findDefinition(updateRequestDto.formFlowDefinitionId) == null) {
            throw RuntimeException("FormFlow definition not found with id ${updateRequestDto.formFlowDefinitionId}")
        }
        return FormFlowProcessLink(
            id = updateRequestDto.id,
            processDefinitionId = processLinkToUpdate.processDefinitionId,
            activityId = processLinkToUpdate.activityId,
            activityType = processLinkToUpdate.activityType,
            formFlowDefinitionId = updateRequestDto.formFlowDefinitionId
        )
    }

    companion object {
        const val PROCESS_LINK_TYPE_FORM_FLOW = "form-flow"
    }
}
