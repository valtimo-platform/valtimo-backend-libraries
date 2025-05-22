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

package com.ritense.processlink.service

import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.domain.ProcessLinkType
import com.ritense.processlink.domain.SupportedProcessLinkTypeHandler
import com.ritense.processlink.exception.ProcessLinkExistsException
import com.ritense.processlink.exception.ProcessLinkNotFoundException
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrElse

@Transactional(readOnly = true)
@Service
@SkipComponentScan
class ProcessLinkService(
    private val processLinkRepository: ProcessLinkRepository,
    private val processLinkMappers: List<ProcessLinkMapper>,
    private val processLinkTypes: List<SupportedProcessLinkTypeHandler>,
    private val camundaRepositoryService: CamundaRepositoryService,
) {

    fun <T : ProcessLink> getProcessLink(
        @LoggableResource(resourceType = ProcessLink::class) processLinkId: UUID,
        clazz: Class<T>
    ): T {
        val processLink = processLinkRepository.findByIdOrNull(processLinkId)
            ?: throw ProcessLinkNotFoundException("For id $processLinkId")

        return try {
            clazz.cast(processLink)
        } catch (e: ClassCastException) {
            throw IllegalStateException("Failed to get process link by id '$processLinkId'", e)
        }
    }

    fun getProcessLinks(
        @LoggableResource(resourceType = CamundaProcessDefinition::class) processDefinitionId: String,
        activityId: String
    ): List<ProcessLink> {
        return processLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinitionId, activityId)
    }

    fun getProcessLinks(
        @LoggableResource(resourceType = CamundaProcessDefinition::class) processDefinitionId: String
    ): List<ProcessLink> {
        return processLinkRepository.findByProcessDefinitionId(processDefinitionId)
    }

    fun getProcessLinksByProcessDefinitionKey(
        @LoggableResource("processDefinitionKey") processDefinitionKey: String
    ): List<ProcessLink> {
        return camundaRepositoryService.findProcessDefinitions(byKey(processDefinitionKey).and(byLatestVersion()))
            .flatMap { processLinkRepository.findByProcessDefinitionId(it.id) }
    }

    fun getProcessLinksByProcessDefinitionIdAndActivityType(
        @LoggableResource(resourceType = CamundaProcessDefinition::class) processDefinitionId: String,
        activityType: ActivityTypeWithEventName
    ): ProcessLink? {
        return processLinkRepository.findByProcessDefinitionIdAndActivityType(processDefinitionId, activityType)
    }

    @Transactional(noRollbackFor = [ProcessLinkExistsException::class])
    @Throws(ProcessLinkExistsException::class)
    fun createProcessLink(createRequest: ProcessLinkCreateRequestDto): ProcessLink {
        return withLoggingContext(CamundaProcessDefinition::class, createRequest.processDefinitionId) {
            val mapper = getProcessLinkMapper(createRequest.processLinkType)
            val newProcessLink = mapper.toNewProcessLink(createRequest)

            val currentProcessLinks = getProcessLinks(createRequest.processDefinitionId, createRequest.activityId)
            if (currentProcessLinks.isNotEmpty()) {
                val contentsDiffer = currentProcessLinks.any { processLinkEntity ->
                    newProcessLink.copy(id = processLinkEntity.id) != processLinkEntity
                }

                throw ProcessLinkExistsException(
                    "A process-link for process-definition '${createRequest.processDefinitionId}' and activity '${createRequest.activityId}' already exists!",
                    contentsDiffer,
                    currentProcessLinks.first().id
                )
            }

            processLinkRepository.save(mapper.toNewProcessLink(createRequest))
        }
    }

    @Transactional
    fun updateProcessLink(updateRequest: ProcessLinkUpdateRequestDto): ProcessLink {
        return withLoggingContext(ProcessLink::class, updateRequest.id) {
            val processLinkToUpdate = processLinkRepository.findById(updateRequest.id)
                .getOrElse { throw IllegalStateException("No ProcessLink found with id ${updateRequest.id}") }
            val mapper = getProcessLinkMapper(updateRequest.processLinkType)
            val processLinkUpdated = mapper.toUpdatedProcessLink(processLinkToUpdate, updateRequest)
            if (processLinkToUpdate::class != processLinkUpdated::class) {
                // Hibernate does not allow 2 different objects with the same identifier in the session, so do delete + insert instead
                processLinkRepository.delete(processLinkToUpdate)
            }
            processLinkRepository.save(processLinkUpdated)
        }
    }

    @Transactional
    fun deleteProcessLink(
        @LoggableResource(resourceType = ProcessLink::class) id: UUID
    ) {
        processLinkRepository.deleteById(id)
    }

    fun getProcessLinkMapper(processLinkType: String): ProcessLinkMapper {
        return processLinkMappers.singleOrNull { it.supportsProcessLinkType(processLinkType) }
            ?: throw IllegalStateException("No ProcessLinkMapper found for processLinkType $processLinkType")
    }

    fun getImporterDependsOnTypes(): Set<String> {
        return processLinkMappers.mapNotNull {
            it.getImporterType()
        }.toSet()
    }

    fun getSupportedProcessLinkTypes(activityType: String): List<ProcessLinkType> {
        if (!ActivityTypeWithEventName.contains(activityType)) {
            return emptyList()
        }
        return processLinkTypes.mapNotNull {
            it.getProcessLinkType(activityType)
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
