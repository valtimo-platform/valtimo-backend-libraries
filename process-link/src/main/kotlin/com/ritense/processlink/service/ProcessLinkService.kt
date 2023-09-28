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
import com.ritense.processlink.exception.ProcessLinkNotFoundException
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import java.util.UUID
import kotlin.jvm.optionals.getOrElse
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
open class ProcessLinkService(
    private val processLinkRepository: ProcessLinkRepository,
    private val processLinkMappers: List<ProcessLinkMapper>,
    private val processLinkTypes: List<SupportedProcessLinkTypeHandler>,
    private val camundaRepositoryService: CamundaRepositoryService,
) {

    fun <T : ProcessLink> getProcessLink(processLinkId: UUID, clazz: Class<T>): T {
        val processLink = processLinkRepository.findByIdOrNull(processLinkId)
            ?: throw ProcessLinkNotFoundException("For id $processLinkId")

        return try {
            clazz.cast(processLink)
        } catch (e: ClassCastException) {
            throw IllegalStateException("Failed to get process link by id '$processLinkId'", e)
        }
    }

    fun getProcessLinks(processDefinitionId: String, activityId: String): List<ProcessLink> {
        return processLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinitionId, activityId)
    }

    fun getProcessLinksByProcessDefinitionKey(processDefinitionKey: String): List<ProcessLink> {
        return camundaRepositoryService.findProcessDefinitions(byKey(processDefinitionKey))
            .flatMap { processLinkRepository.findByProcessDefinitionId(it.id) }
    }

    fun getProcessLinksByProcessDefinitionIdAndActivityType(processDefinitionId: String, activityType: ActivityTypeWithEventName): ProcessLink? {
        return processLinkRepository.findByProcessDefinitionIdAndActivityType(processDefinitionId, activityType)
    }

    @Transactional
    @Throws(ProcessLinkExistsException::class)
    fun createProcessLink(createRequest: ProcessLinkCreateRequestDto): ProcessLink {
        val mapper = getProcessLinkMapper(createRequest.processLinkType)
        val newProcessLink = mapper.toNewProcessLink(createRequest)

        val currentProcessLinks = getProcessLinks(createRequest.processDefinitionId, createRequest.activityId)
        if (currentProcessLinks.isNotEmpty()) {
            val contentsDiffer = currentProcessLinks.any { processLinkEntity ->
                newProcessLink.copy(id = processLinkEntity.id) != processLinkEntity
            }

            throw ProcessLinkExistsException(
                "A process-link for process-definition '${createRequest.processDefinitionId}' and activity '${createRequest.activityId}' already exists!",
                contentsDiffer
            )
        }

        return processLinkRepository.save(mapper.toNewProcessLink(createRequest))
    }

    @Transactional
    fun updateProcessLink(updateRequest: ProcessLinkUpdateRequestDto): ProcessLink {
        val processLinkToUpdate = processLinkRepository.findById(updateRequest.id)
            .getOrElse { throw IllegalStateException("No ProcessLink found with id ${updateRequest.id}") }
        check(updateRequest.processLinkType == processLinkToUpdate.processLinkType) {
            "The processLinkType of the persisted entity does not match the given type!"
        }
        val mapper = getProcessLinkMapper(processLinkToUpdate.processLinkType)
        val processLinkUpdated = mapper.toUpdatedProcessLink(processLinkToUpdate, updateRequest)
        return processLinkRepository.save(processLinkUpdated)
    }

    @Transactional
    fun deleteProcessLink(id: UUID) {
        processLinkRepository.deleteById(id)
    }

    fun getProcessLinkMapper(processLinkType: String): ProcessLinkMapper {
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
