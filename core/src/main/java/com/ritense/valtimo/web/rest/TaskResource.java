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

package com.ritense.valtimo.web.rest;

import static com.ritense.logging.LoggingContextKt.withLoggingContext;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.ritense.logging.LoggableResource;
import com.ritense.valtimo.operaton.domain.OperatonTask;
import com.ritense.valtimo.operaton.dto.TaskExtended;
import com.ritense.valtimo.contract.annotation.SkipComponentScan;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import com.ritense.valtimo.service.OperatonProcessService;
import com.ritense.valtimo.service.OperatonTaskService;
import com.ritense.valtimo.service.request.AssigneeRequest;
import com.ritense.valtimo.service.request.SetDueDateRequest;
import com.ritense.valtimo.web.rest.dto.BatchAssignTaskDTO;
import com.ritense.valtimo.web.rest.dto.CustomTaskDto;
import com.ritense.valtimo.web.rest.dto.TaskCompletionDTO;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.util.List;
import javax.annotation.Nullable;
import org.operaton.bpm.engine.FormService;
import org.operaton.bpm.engine.task.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@RestController
@SkipComponentScan
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class TaskResource extends AbstractTaskResource {

    public TaskResource(
        final FormService formService,
        final OperatonTaskService operatonTaskService,
        final OperatonProcessService operatonProcessService
    ) {
        super(formService, operatonTaskService, operatonProcessService);
    }

    /**
     * Endpoint that return a list of tasks.
     *
     * @deprecated since 12.0.0, use v2 instead
     */
    @GetMapping("/v1/task")
    @Deprecated(since = "12.0.0", forRemoval = true)
    public ResponseEntity<List<TaskExtended>> getTasks(
        @RequestParam OperatonTaskService.TaskFilter filter,
        @PageableDefault(sort = {"created"}, direction = DESC) Pageable pageable
    ) throws Exception {
        var page = operatonTaskService.findTasksFiltered(filter, pageable);
        var headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/task");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/v2/task")
    public ResponseEntity<Page<TaskExtended>> getTasksPaged(
        @RequestParam OperatonTaskService.TaskFilter filter,
        @PageableDefault(sort = {"created"}, direction = DESC) Pageable pageable
    ) {
        var page = operatonTaskService.findTasksFiltered(filter, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/v1/task/{taskId}")
    public ResponseEntity<CustomTaskDto> getTask(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId,
        HttpServletRequest request
    ) {
        CustomTaskDto customTaskDto;
        try {
            customTaskDto = createCustomTaskDto(taskId, request);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customTaskDto);
    }

    @PostMapping("/v1/task/{taskId}/assign")
    public ResponseEntity<Void> assign(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId,
        @RequestBody AssigneeRequest assigneeRequest
    ) {
        operatonTaskService.assign(taskId, assigneeRequest.getAssignee());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/assign/batch-assign")
    public ResponseEntity<Void> batchClaim(@RequestBody BatchAssignTaskDTO batchAssignTaskDTO) {
        final String assignee = batchAssignTaskDTO.getAssignee();
        batchAssignTaskDTO.getTasksIds().forEach(taskId -> operatonTaskService.assign(taskId, assignee));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/{taskId}/unassign")
    public ResponseEntity<Void> unassign(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId
    ) {
        operatonTaskService.unassign(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/{taskId}/complete")
    public ResponseEntity<Void> complete(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId,
        @RequestBody TaskCompletionDTO taskCompletionDTO
    ) {
        operatonTaskService.completeTaskAndDeleteFiles(taskId, taskCompletionDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/batch-complete")
    public ResponseEntity<Void> batchComplete(@RequestBody List<String> taskIdList) {
        taskIdList.forEach(taskId -> {
            withLoggingContext(OperatonTask.class, taskId, () -> {
                if (!operatonTaskService.hasTaskFormData(taskId)) {
                    operatonTaskService.complete(taskId);
                }
            });
        });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/task/{taskId}/set-due-date")
    public ResponseEntity<Void> setDueDate(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId,
        @RequestBody @Nullable SetDueDateRequest setDueDateRequest
    ) {
        operatonTaskService.setDueDate(
            taskId,
            (setDueDateRequest != null) ? setDueDateRequest.getDueDate() : null
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Retrieve a list of comments that are associated to the task and to the process instance.
     *
     * @deprecated Task comments will be removed in the future.
     */
    @Deprecated(since = "11.1.0", forRemoval = true)
    @GetMapping("/v1/task/{taskId}/comments")
    public ResponseEntity<List<Comment>> getProcessInstanceComments(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId
    ) {
        final OperatonTask task = operatonTaskService.findTaskById(taskId);
        List<Comment> taskComments = operatonTaskService.getTaskComments(task.getId());
        taskComments.addAll(operatonTaskService.getProcessInstanceComments(task.getProcessInstanceId()));
        taskComments.sort((Comment c1, Comment c2) -> c2.getTime().compareTo(c1.getTime()));
        return ResponseEntity.ok(taskComments);
    }

    @Deprecated(since = "10.8.0", forRemoval = true)
    @GetMapping("/v1/task/{taskId}/candidate-user")
    public ResponseEntity<List<ManageableUser>> getTaskCandidateUsers(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId
    ) {
        List<ManageableUser> users = operatonTaskService.getCandidateUsers(taskId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/v2/task/{taskId}/candidate-user")
    public ResponseEntity<List<NamedUser>> getNamedCandidateUsers(
        @LoggableResource(resourceType = OperatonTask.class) @PathVariable String taskId
    ) {
        List<NamedUser> users = operatonTaskService.getNamedCandidateUsers(taskId);
        return ResponseEntity.ok(users);
    }

    // Overriding the default TaskFilter binder so it's not case sensitive
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(OperatonTaskService.TaskFilter.class, new CaseInsensitiveTaskFilterEditor());
    }

    private static class CaseInsensitiveTaskFilterEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(OperatonTaskService.TaskFilter.valueOf(text.toUpperCase()));
        }
    }

}
