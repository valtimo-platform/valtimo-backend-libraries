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

package com.ritense.case.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.case.service.validations.SaveTaskListColumnValidator
import com.ritense.case.service.validations.ListColumnValidator
import com.ritense.case.service.validations.Operation
import com.ritense.case.web.rest.dto.TaskListColumnDto
import com.ritense.case.web.rest.mapper.TaskListColumnMapper
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valueresolver.ValueResolverService
import kotlin.jvm.optionals.getOrNull
import org.springframework.transaction.annotation.Transactional

@Transactional
class TaskColumnService(
    private val taskListColumnRepository: TaskListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    valueResolverService: ValueResolverService,
    private val authorizationService: AuthorizationService
) {
    var validators: Map<Operation, ListColumnValidator<TaskListColumnDto>> = mapOf(
        Operation.CREATE to SaveTaskListColumnValidator(
            taskListColumnRepository,
            documentDefinitionService,
            valueResolverService
        )
    )

    @Throws(InvalidListColumnException::class)
    fun saveListColumn(
        caseDefinitionName: String,
        taskListColumnDto: TaskListColumnDto
    ) {
        denyManagementOperation()

        runWithoutAuthorization {
            validators[Operation.CREATE]!!.validate(caseDefinitionName, taskListColumnDto)
        }
        taskListColumnDto.order = taskListColumnRepository.countByIdCaseDefinitionName(caseDefinitionName)
        taskListColumnRepository
            .save(TaskListColumnMapper.toEntity(caseDefinitionName, taskListColumnDto))
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun getListColumns(caseDefinitionName: String): List<TaskListColumnDto> {
        // TODO: Implement PBAC:
        // It currently relies on the VIEW check in findLatestByName via assertDocumentDefinitionExists.
        // Doing a check here forces this class to be a JsonSchemaDocument implementation, which is undesirable.
        assertDocumentDefinitionExists(caseDefinitionName)

        return TaskListColumnMapper
            .toDtoList(
                taskListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
                    caseDefinitionName
                )
            )
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun deleteTaskListColumn(caseDefinitionName: String, columnKey: String) {
        denyManagementOperation()

        runWithoutAuthorization { assertDocumentDefinitionExists(caseDefinitionName) }

        if (taskListColumnRepository
                .existsByIdCaseDefinitionNameAndIdKey(caseDefinitionName, columnKey)
        ) {
            taskListColumnRepository.deleteByIdCaseDefinitionNameAndIdKey(caseDefinitionName, columnKey)
        }
    }

    private fun denyManagementOperation() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                Any::class.java,
                Action.deny()
            )
        )
    }

    @Throws(UnknownDocumentDefinitionException::class)
    private fun assertDocumentDefinitionExists(caseDefinitionName: String): DocumentDefinition {
        return documentDefinitionService.findLatestByName(caseDefinitionName)
            .getOrNull() ?: throw UnknownCaseDefinitionException(caseDefinitionName)
    }
}
