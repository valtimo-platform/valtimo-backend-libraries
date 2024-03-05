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

package com.ritense.case.web.rest.mapper

import com.ritense.case.domain.TaskListColumn
import com.ritense.case.domain.TaskListColumnId
import com.ritense.case.web.rest.dto.TaskListColumnDto

class TaskListColumnMapper {
    companion object {

        fun toEntity(caseDefinitionName: String, taskListColumnDto: TaskListColumnDto): TaskListColumn {
            return TaskListColumn(
                TaskListColumnId(caseDefinitionName, taskListColumnDto.key),
                taskListColumnDto.title,
                taskListColumnDto.path,
                taskListColumnDto.displayType,
                taskListColumnDto.sortable,
                taskListColumnDto.defaultSort,
                taskListColumnDto.order!!
            )
        }

        fun toDto(taskListColumn: TaskListColumn): TaskListColumnDto {
            return TaskListColumnDto(
                taskListColumn.title,
                taskListColumn.id.key,
                taskListColumn.path,
                taskListColumn.displayType,
                taskListColumn.sortable,
                taskListColumn.defaultSort,
                taskListColumn.order
            )
        }

        fun toEntityList(caseDefinitionName: String, taskListColumns: List<TaskListColumnDto>): List<TaskListColumn> {
            return taskListColumns.stream().map { column ->
                toEntity(caseDefinitionName, column)
            }.toList()
        }

        fun toDtoList(taskListColumns: List<TaskListColumn>): List<TaskListColumnDto> {
            return taskListColumns.stream().map { column ->
                toDto(column)
            }.toList()
        }
    }
}