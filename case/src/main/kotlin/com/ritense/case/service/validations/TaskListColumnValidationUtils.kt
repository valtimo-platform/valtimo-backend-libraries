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

package com.ritense.case.service.validations

import com.ritense.case.domain.TaskListColumn
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.case.web.rest.dto.TaskListColumnDto
import com.ritense.case.web.rest.mapper.TaskListColumnMapper
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valueresolver.ValueResolverService
import org.zalando.problem.Status

private const val TASK_PREFIX = "task:"

open class TaskListColumnValidationUtils(
    open val taskListColumnRepository: TaskListColumnRepository,
    open val documentDefinitionService: DocumentDefinitionService,
    open val valueResolverService: ValueResolverService,
) {

    @Throws(InvalidListColumnException::class)
    internal fun isColumnDefaultSortValid(caseDefinitionName: String, taskListColumnDto: TaskListColumnDto) {

        if (taskListColumnDto.defaultSort != null) {
            val customTaskColumnsForCase =
                taskListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(caseDefinitionName)

            if (customTaskColumnsForCase.filter { taskListColumnDto.key != it.id.key }
                    .any { column -> column.defaultSort != null }) {
                throw InvalidListColumnException(
                    "Unable to save list column. Another column with defaultSort value already exists",
                    Status.BAD_REQUEST
                )
            }
        }
    }

    @Throws(InvalidListColumnException::class)
    internal fun assertDocumentDefinitionExists(documentDefinitionName: String) {
        try {
            documentDefinitionService.findIdByName(documentDefinitionName)
        } catch (ex: UnknownDocumentDefinitionException) {
            throw UnknownCaseDefinitionException(ex.message)
        }
    }

    @Throws
    internal fun overrideListColumnDtoWithDefaultSort(
        caseDefinitionName: String,
        taskListColumnDtoList: List<TaskListColumnDto>,
        columns: List<TaskListColumn>
    ) {
        taskListColumnDtoList.forEach { taskListColumnDto ->
            if (existsColumnWithDefaultSort(taskListColumnDto, columns)) {
                val column = TaskListColumnMapper.toDto(columns.first { column -> column.defaultSort != null })
                column.defaultSort = null
                taskListColumnRepository.save(TaskListColumnMapper.toEntity(caseDefinitionName, column))
            }
        }
    }

    internal fun existsColumnWithDefaultSort(
        taskListColumnDto: TaskListColumnDto, columns: List<TaskListColumn>
    ): Boolean {
        return taskListColumnDto.defaultSort != null
            && columns.any { column -> column.defaultSort != null }
    }

    @Throws(InvalidListColumnException::class)
    internal fun isPropertyPathValid(caseDefinitionName: String, taskListColumnDto: TaskListColumnDto) {
        val path = taskListColumnDto.path

        if (path.startsWith(TASK_PREFIX, ignoreCase = true)) {
            val pathWithoutPrefix = path.substring(TASK_PREFIX.length)
            if (!listOf("createTime", "name", "assignee", "dueDate").contains(pathWithoutPrefix)) {
                throw InvalidListColumnException("\"${pathWithoutPrefix}\" is not an option for the task: prefix.", Status.BAD_REQUEST)
            }
        } else {
            try {
                valueResolverService.validateValues(caseDefinitionName, listOf(path))
            } catch (ex: Exception) {
                throw InvalidListColumnException(ex.message, Status.BAD_REQUEST)
            }
        }
    }

}
