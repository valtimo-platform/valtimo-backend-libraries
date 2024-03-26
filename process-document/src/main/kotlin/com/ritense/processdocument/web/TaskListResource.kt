package com.ritense.processdocument.web

import com.ritense.processdocument.service.CaseTaskListSearchService
import com.ritense.processdocument.web.request.TaskListSearchDto
import com.ritense.valtimo.camunda.dto.TaskExtended
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
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
}