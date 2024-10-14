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

package com.ritense.valtimo.formflow.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exporter.request.ExportRequest
import com.ritense.exporter.request.FormFlowDefinitionExportRequest
import com.ritense.formflow.service.FormFlowService
import com.ritense.logging.withLoggingContext
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.formflow.domain.FormFlowProcessLink
import com.ritense.valtimo.formflow.processlink.dto.FormFlowProcessLinkDeployDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkExportResponseDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkResponseDto
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkUpdateRequestDto
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@SkipComponentScan
class FormFlowProcessLinkMapper(
    objectMapper: ObjectMapper,
    private val formFlowService: FormFlowService,
) : ProcessLinkMapper {

    init {
        objectMapper.registerSubtypes(
            FormFlowProcessLinkDeployDto::class.java,
            FormFlowProcessLinkResponseDto::class.java,
            FormFlowProcessLinkCreateRequestDto::class.java,
            FormFlowProcessLinkUpdateRequestDto::class.java,
            FormFlowProcessLinkExportResponseDto::class.java
        )
    }

    override fun supportsProcessLinkType(processLinkType: String) = processLinkType == PROCESS_LINK_TYPE_FORM_FLOW

    override fun toProcessLinkResponseDto(
        processLink: ProcessLink
    ): ProcessLinkResponseDto {
        return withLoggingContext(ProcessLink::class, processLink.id) {
            processLink as FormFlowProcessLink
            FormFlowProcessLinkResponseDto(
                id = processLink.id,
                processDefinitionId = processLink.processDefinitionId,
                activityId = processLink.activityId,
                activityType = processLink.activityType,
                formFlowDefinitionId = processLink.formFlowDefinitionId,
                formDisplayType = processLink.formDisplayType,
                formSize = processLink.formSize,
            )
        }
    }

    override fun toProcessLinkCreateRequestDto(deployDto: ProcessLinkDeployDto): ProcessLinkCreateRequestDto {
        return withLoggingContext(CamundaProcessDefinition::class, deployDto.processDefinitionId) {
            deployDto as FormFlowProcessLinkDeployDto

            FormFlowProcessLinkCreateRequestDto(
                processDefinitionId = deployDto.processDefinitionId,
                activityId = deployDto.activityId,
                activityType = deployDto.activityType,
                formFlowDefinitionId = deployDto.formFlowDefinitionId,
                formDisplayType = deployDto.formDisplayType,
                formSize = deployDto.formSize,
            )
        }
    }

    override fun toProcessLinkUpdateRequestDto(
        deployDto: ProcessLinkDeployDto,
        existingProcessLinkId: UUID
    ): ProcessLinkUpdateRequestDto {
        return withLoggingContext(CamundaProcessDefinition::class, deployDto.processDefinitionId) {
            deployDto as FormFlowProcessLinkDeployDto

            FormFlowProcessLinkUpdateRequestDto(
                id = existingProcessLinkId,
                formFlowDefinitionId = deployDto.formFlowDefinitionId
            )
        }
    }

    override fun toProcessLinkExportResponseDto(
        processLink: ProcessLink
    ): ProcessLinkExportResponseDto {
        return withLoggingContext(ProcessLink::class, processLink.id) {
            processLink as FormFlowProcessLink
            FormFlowProcessLinkExportResponseDto(
                activityId = processLink.activityId,
                activityType = processLink.activityType,
                formFlowDefinitionId = "${processLink.formFlowDefinitionId.substringBeforeLast(":")}:latest",
                formDisplayType = processLink.formDisplayType,
                formSize = processLink.formSize,
            )
        }
    }

    override fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink {
        return withLoggingContext(ProcessDefinition::class, createRequestDto.processDefinitionId) {
            createRequestDto as FormFlowProcessLinkCreateRequestDto
            if (formFlowService.findDefinition(createRequestDto.formFlowDefinitionId) == null) {
                throw RuntimeException("FormFlow definition not found with id ${createRequestDto.formFlowDefinitionId}")
            }
            FormFlowProcessLink(
                id = UUID.randomUUID(),
                processDefinitionId = createRequestDto.processDefinitionId,
                activityId = createRequestDto.activityId,
                activityType = createRequestDto.activityType,
                formFlowDefinitionId = createRequestDto.formFlowDefinitionId,
                formDisplayType = createRequestDto.formDisplayType,
                formSize = createRequestDto.formSize,
            )
        }
    }

    override fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink {
        return withLoggingContext(ProcessLink::class, processLinkToUpdate.id) {
            updateRequestDto as FormFlowProcessLinkUpdateRequestDto
            if (formFlowService.findDefinition(updateRequestDto.formFlowDefinitionId) == null) {
                throw RuntimeException("FormFlow definition not found with id ${updateRequestDto.formFlowDefinitionId}")
            }
            FormFlowProcessLink(
                id = updateRequestDto.id,
                processDefinitionId = processLinkToUpdate.processDefinitionId,
                activityId = processLinkToUpdate.activityId,
                activityType = processLinkToUpdate.activityType,
                formFlowDefinitionId = updateRequestDto.formFlowDefinitionId,
                formDisplayType = updateRequestDto.formDisplayType,
                formSize = updateRequestDto.formSize,
            )
        }
    }

    override fun createRelatedExportRequests(
        processLink: ProcessLink
    ): Set<ExportRequest> {
        return withLoggingContext(ProcessLink::class, processLink.id) {
            processLink as FormFlowProcessLink
            setOf(FormFlowDefinitionExportRequest(processLink.formFlowDefinitionId))
        }
    }

    override fun getImporterType() = "formflow"

    companion object {
        const val PROCESS_LINK_TYPE_FORM_FLOW = "form-flow"
    }
}
