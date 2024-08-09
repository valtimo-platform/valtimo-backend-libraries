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

package com.ritense.processdocument.web

import com.ritense.processdocument.service.CaseTaskListSearchService
import com.ritense.processdocument.tasksearch.SearchWithConfigRequest
import com.ritense.processdocument.web.request.TaskListSearchDto
import com.ritense.processdocument.web.result.TaskListRowDto
import com.ritense.valtimo.camunda.dto.TaskExtended
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class TaskListResource (
    private val service: CaseTaskListSearchService,
    private val camundaTaskService: CamundaTaskService
) {

    @PostMapping("/v3/task")
    fun getTaskList(
        @RequestParam("filter") assignmentFilter: CamundaTaskService.TaskFilter,
        @RequestBody taskListSearchDto: TaskListSearchDto,
        pageable: Pageable
    ): ResponseEntity<Page<*>> {
        return if (taskListSearchDto.caseDefinitionName != null) {
            ResponseEntity.ok().body(service.getTasksByCaseDefinition(taskListSearchDto.caseDefinitionName, assignmentFilter, pageable))
        } else {
            val page: Page<TaskExtended> = camundaTaskService.findTasksFiltered(assignmentFilter, pageable)
            return ResponseEntity.ok(page)
        }
    }

    @PostMapping("/v1/document-definition/{caseDefinitionName}/task/search")
    fun searchTaskList(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @RequestBody searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): ResponseEntity<Page<TaskListRowDto>> {
        val result = service.searchTaskListRows(caseDefinitionName, searchRequest, pageable)
        return ResponseEntity.ok(result)
    }

}