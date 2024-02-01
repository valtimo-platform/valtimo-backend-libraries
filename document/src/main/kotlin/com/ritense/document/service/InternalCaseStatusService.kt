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

package com.ritense.document.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.InternalCaseStatus
import com.ritense.document.domain.InternalCaseStatusId
import com.ritense.document.exception.InternalCaseStatusAlreadyExistsException
import com.ritense.document.repository.InternalCaseStatusRepository
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateOrderRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateRequestDto
import kotlin.jvm.optionals.getOrNull

class InternalCaseStatusService(
    private val internalCaseStatusRepository: InternalCaseStatusRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    private val authorizationService: AuthorizationService,
) {
    fun getInternalCaseStatuses(documentDefinitionName: String): List<InternalCaseStatus> {
        denyManagementOperation()
        return internalCaseStatusRepository.findById_CaseDefinitionNameOrderByOrder(documentDefinitionName)
    }

    fun create(
        caseDefinitionName: String,
        request: InternalCaseStatusCreateRequestDto
    ): InternalCaseStatus {
        denyManagementOperation()

        documentDefinitionService.findLatestByName(caseDefinitionName).getOrNull()
            ?: throw NoSuchElementException("Case definition with name $caseDefinitionName does not exist!")
        val currentInternalCaseStatuses = getInternalCaseStatuses(caseDefinitionName)
        if (currentInternalCaseStatuses.any { status ->
                status.id.key == request.key
            }) {
            throw InternalCaseStatusAlreadyExistsException(request.key)
        }

        return internalCaseStatusRepository.save(
            InternalCaseStatus(
                InternalCaseStatusId(
                    request.key,
                    caseDefinitionName
                ),
                request.title,
                request.visibleInCaseListByDefault,
                currentInternalCaseStatuses.size
            )
        )
    }

    fun update(
        caseDefinitionName: String,
        internalCaseStatusKey: String,
        requestDto: InternalCaseStatusUpdateRequestDto,
    ) {
        denyManagementOperation()

        internalCaseStatusRepository.findDistinctById_CaseDefinitionNameAndId_Key(
            caseDefinitionName, internalCaseStatusKey
        )?.let {
            internalCaseStatusRepository.save(
                it.copy(
                    title = requestDto.title,
                    visibleInCaseListByDefault = requestDto.visibleInCaseListByDefault
                )
            )
        }
    }

    fun update(
        caseDefinitionName: String,
        requestDtos: List<InternalCaseStatusUpdateOrderRequestDto>
    ): List<InternalCaseStatus> {
        denyManagementOperation()

        val existingInternalCaseStatuses = internalCaseStatusRepository
            .findById_CaseDefinitionNameOrderByOrder(caseDefinitionName)
        check(existingInternalCaseStatuses.size == requestDtos.size) {
            throw IllegalStateException(
                "Failed to update internal case statuses. Reason: the number of internal "
                    + "case statuses in the update request does not match the number of existing internal case statuses."
            )
        }

        val updatedInternalCaseStatuses = requestDtos.mapIndexed { index, requestDto ->
            val existingInternalCaseStatus = existingInternalCaseStatuses.find { it.id.key == requestDto.key }
                ?: throw IllegalStateException(
                    "Failed to update internal case statuses. Reason: internal case "
                        + "status with key '${requestDto.key}' does not exist."
                )
            existingInternalCaseStatus.copy(
                title = requestDto.title,
                order = index,
                visibleInCaseListByDefault = requestDto.visibleInCaseListByDefault
            )
        }

        return internalCaseStatusRepository.saveAll(updatedInternalCaseStatuses)
    }

    fun delete(caseDefinitionName: String, internalCaseStatusKey: String) {
        denyManagementOperation()

        internalCaseStatusRepository.findDistinctById_CaseDefinitionNameAndId_Key(
            caseDefinitionName, internalCaseStatusKey
        )?.let {
            internalCaseStatusRepository.delete(it)
            reorder(caseDefinitionName)
        }
    }

    private fun reorder(caseDefinitionName: String) {
        val internalCaseStatuses = internalCaseStatusRepository.findById_CaseDefinitionNameOrderByOrder(
            caseDefinitionName
        ).mapIndexed { index, internalCaseStatus -> internalCaseStatus.copy(order = index) }
        internalCaseStatusRepository.saveAll(internalCaseStatuses)
    }

    private fun denyManagementOperation() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                Any::class.java,
                Action.deny()
            )
        )
    }
}