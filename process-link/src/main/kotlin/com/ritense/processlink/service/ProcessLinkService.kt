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

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.domain.ProcessLinkType
import com.ritense.processlink.domain.SupportedProcessLinkTypeHandler
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import java.util.UUID
import javax.validation.ValidationException
import kotlin.jvm.optionals.getOrElse
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
open class ProcessLinkService(
    private val processLinkRepository: ProcessLinkRepository,
    private val processLinkMappers: List<ProcessLinkMapper>,
    private val processLinkTypes: List<SupportedProcessLinkTypeHandler>
) {

    fun getProcessLinks(processDefinitionId: String, activityId: String): List<ProcessLink> {
        return processLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinitionId, activityId)
    }

    fun getProcessLinks(
        activityId: String,
        activityType: ActivityTypeWithEventName,
        processLinkType: String
    ): List<ProcessLink> {
        return processLinkRepository.findByActivityIdAndActivityTypeAndProcessLinkType(
            activityId,
            activityType,
            processLinkType
        )
    }

    @Transactional
    fun createProcessLink(createRequest: ProcessLinkCreateRequestDto) {
        if (getProcessLinks(createRequest.processDefinitionId, createRequest.activityId).isNotEmpty()) {
            throw ValidationException("A process-link for process-definition '${createRequest.processDefinitionId}' and activity '${createRequest.activityId}' already exists!")
        }

        val mapper = getProcessLinkMapper(createRequest.processLinkType)
        processLinkRepository.save(mapper.toNewProcessLink(createRequest))
    }

    @Transactional
    fun updateProcessLink(updateRequest: ProcessLinkUpdateRequestDto) {
        val processLinkToUpdate = processLinkRepository.findById(updateRequest.id)
            .getOrElse { throw IllegalStateException("No ProcessLink found with id ${updateRequest.id}") }
        check(updateRequest.processLinkType == processLinkToUpdate.processLinkType) {
            "The processLinkType of the persisted entity does not match the given type!"
        }
        val mapper = getProcessLinkMapper(processLinkToUpdate.processLinkType)
        val processLinkUpdated = mapper.toUpdatedProcessLink(processLinkToUpdate, updateRequest)
        processLinkRepository.save(processLinkUpdated)
    }

    @Transactional
    fun deleteProcessLink(id: UUID) {
        processLinkRepository.deleteById(id)
    }

    private fun getProcessLinkMapper(processLinkType: String): ProcessLinkMapper {
        return processLinkMappers.singleOrNull { it.supportsProcessLinkType(processLinkType) }
            ?: throw IllegalStateException("No ProcessLinkMapper found for processLinkType $processLinkType")
    }

    fun getSupportedProcessLinkTypes(activityType: String): List<ProcessLinkType> {
        return processLinkTypes.mapNotNull {
            it.getProcessLinkType(activityType)
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
