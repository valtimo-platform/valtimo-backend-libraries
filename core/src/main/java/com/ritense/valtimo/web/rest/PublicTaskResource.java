/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.web.rest.dto.CustomTaskDto;
import com.ritense.valtimo.web.rest.dto.TaskCompletionDTO;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/public", produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicTaskResource extends AbstractTaskResource {

    public PublicTaskResource(
        final TaskService taskService,
        final FormService formService,
        final CamundaTaskService camundaTaskService,
        final CamundaProcessService camundaProcessService
    ) {
        super(taskService, formService, camundaTaskService, camundaProcessService);
    }

    @GetMapping(value = "/task/{taskDefinitionId}")
    @ResponseBody
    @PreAuthorize("hasPermission(#taskDefinitionId, 'publicTaskAccess')")
    public ResponseEntity<CustomTaskDto> getTask(
        @PathVariable(name = "taskDefinitionId") final String taskDefinitionId,
        HttpServletRequest request
    ) {
        CustomTaskDto customTaskDto;
        try {
            customTaskDto = createCustomTaskDto(taskDefinitionId, request);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customTaskDto);
    }

    @PostMapping(value = "/task/{taskDefinitionId}/complete")
    @ResponseBody
    @PreAuthorize("hasPermission(#taskDefinitionId, 'publicTaskAccess')")
    public ResponseEntity<Void> completeTaskAndDeleteFiles(
        @PathVariable(name = "taskDefinitionId") final String taskDefinitionId,
        @RequestBody TaskCompletionDTO taskCompletionDTO
    ) {
        camundaTaskService.completeTaskAndDeleteFiles(taskDefinitionId, taskCompletionDTO);
        return ResponseEntity.ok().build();
    }

}
