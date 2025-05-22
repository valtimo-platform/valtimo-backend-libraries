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

package com.ritense.case.web.rest

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.case.service.TaskColumnService
import com.ritense.case.web.rest.dto.TaskListColumnDto
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class TaskListResource(
    private val service: TaskColumnService
) {

    @GetMapping("/v1/case/{caseDefinitionName}/task-list-column")
    @RunWithoutAuthorization
    fun getTaskListColumn(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<TaskListColumnDto>> {
        return ResponseEntity.ok().body(service.getListColumns(caseDefinitionName))
    }

    @GetMapping("/management/v1/case/{caseDefinitionName}/task-list-column")
    @RunWithoutAuthorization
    fun getTaskListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<TaskListColumnDto>> = getTaskListColumn(caseDefinitionName)

    @PutMapping("/management/v1/case/{caseDefinitionName}/task-list-column/{columnKey}")
    @RunWithoutAuthorization
    fun createListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody taskListColumnDto: TaskListColumnDto
    ): ResponseEntity<Any> {
        service.saveListColumn(caseDefinitionName, taskListColumnDto)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/management/v1/case/{caseDefinitionName}/task-list-column")
    @RunWithoutAuthorization
    fun swapColumnOrderForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody taskListColumnDto: Pair<String, String>
    ): ResponseEntity<Any> {
        service.swapColumnOrder(caseDefinitionName, taskListColumnDto.first, taskListColumnDto.second)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/management/v1/case/{caseDefinitionName}/task-list-column/{columnKey}")
    @RunWithoutAuthorization
    fun deleteListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable columnKey: String
    ): ResponseEntity<Any> {
        service.deleteTaskListColumn(caseDefinitionName, columnKey)
        return ResponseEntity.noContent().build()
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
