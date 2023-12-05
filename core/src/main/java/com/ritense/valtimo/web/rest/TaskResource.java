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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.dto.TaskExtended;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.service.request.AssigneeRequest;
import com.ritense.valtimo.web.rest.dto.BatchAssignTaskDTO;
import com.ritense.valtimo.web.rest.dto.CustomTaskDto;
import com.ritense.valtimo.web.rest.dto.TaskCompletionDTO;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.task.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.util.List;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class TaskResource extends AbstractTaskResource {

    public TaskResource(
        final FormService formService,
        final CamundaTaskService camundaTaskService,
        final CamundaProcessService camundaProcessService
    ) {
        super(formService, camundaTaskService, camundaProcessService);
    }

    @GetMapping("/v1/task")
    public ResponseEntity<List<? extends TaskExtended>> getTasks(
        @RequestParam CamundaTaskService.TaskFilter filter,
        Pageable pageable
    ) throws Exception {
        var page = camundaTaskService.findTasksFiltered(filter, pageable);
        var headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/task");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/v1/task/{taskId}")
    public ResponseEntity<CustomTaskDto> getTask(@PathVariable String taskId, HttpServletRequest request) {
        CustomTaskDto customTaskDto;
        try {
            customTaskDto = createCustomTaskDto(taskId, request);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customTaskDto);
    }

    @PostMapping("/v1/task/{taskId}/assign")
    public ResponseEntity<Void> assign(@PathVariable String taskId, @RequestBody AssigneeRequest assigneeRequest) {
        camundaTaskService.assign(taskId, assigneeRequest.getAssignee());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/assign/batch-assign")
    public ResponseEntity<Void> batchClaim(@RequestBody BatchAssignTaskDTO batchAssignTaskDTO) {
        final String assignee = batchAssignTaskDTO.getAssignee();
        batchAssignTaskDTO.getTasksIds().forEach(taskId -> camundaTaskService.assign(taskId, assignee));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/{taskId}/unassign")
    public ResponseEntity<Void> unassign(@PathVariable String taskId) {
        camundaTaskService.unassign(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/{taskId}/complete")
    public ResponseEntity<Void> complete(
        @PathVariable String taskId,
        @RequestBody TaskCompletionDTO taskCompletionDTO
    ) {
        camundaTaskService.completeTaskAndDeleteFiles(taskId, taskCompletionDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/batch-complete")
    public ResponseEntity<Void> batchComplete(@RequestBody List<String> taskIdList) {
        taskIdList.forEach(taskId -> {
            if (!camundaTaskService.hasTaskFormData(taskId)) {
                camundaTaskService.complete(taskId);
            }
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/task/{taskId}/comments")
    public ResponseEntity<List<Comment>> getProcessInstanceComments(@PathVariable String taskId) {
        final CamundaTask task = camundaTaskService.findTaskById(taskId);
        List<Comment> taskComments = camundaTaskService.getTaskComments(task.getId());
        taskComments.addAll(camundaTaskService.getProcessInstanceComments(task.getProcessInstanceId()));
        taskComments.sort((Comment c1, Comment c2) -> c2.getTime().compareTo(c1.getTime()));
        return ResponseEntity.ok(taskComments);
    }

    @Deprecated(since = "10.8.0", forRemoval = true)
    @GetMapping("/v1/task/{taskId}/candidate-user")
    public ResponseEntity<List<ManageableUser>> getTaskCandidateUsers(@PathVariable String taskId) {
        List<ManageableUser> users = camundaTaskService.getCandidateUsers(taskId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/v2/task/{taskId}/candidate-user")
    public ResponseEntity<List<NamedUser>> getNamedCandidateUsers(@PathVariable String taskId) {
        List<NamedUser> users = camundaTaskService.getNamedCandidateUsers(taskId);
        return ResponseEntity.ok(users);
    }

    // Overriding the default TaskFilter binder so it's not case sensitive
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(CamundaTaskService.TaskFilter.class, new CaseInsensitiveTaskFilterEditor());
    }

    private static class CaseInsensitiveTaskFilterEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(CamundaTaskService.TaskFilter.valueOf(text.toUpperCase()));
        }
    }

}
