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

import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.service.util.FormUtils;
import com.ritense.valtimo.web.rest.dto.CustomTaskDto;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTaskResource {

    protected final TaskService taskService;
    final FormService formService;
    final CamundaTaskService camundaTaskService;
    private final CamundaProcessService camundaProcessService;

    AbstractTaskResource(
        final TaskService taskService,
        final FormService formService,
        final CamundaTaskService camundaTaskService,
        final CamundaProcessService camundaProcessService
    ) {
        this.taskService = taskService;
        this.formService = formService;
        this.camundaTaskService = camundaTaskService;
        this.camundaProcessService = camundaProcessService;
    }

    public CustomTaskDto createCustomTaskDto(String id, HttpServletRequest request) {
        final Task task = camundaTaskService.findTaskById(id);
        TaskDto taskDto = TaskDto.fromEntity(task);

        ProcessInstance processInstance = camundaProcessService.findProcessInstanceById(taskDto.getProcessInstanceId()).orElseThrow();
        ProcessDefinition processDefinition = camundaProcessService.findProcessDefinitionById(processInstance.getProcessDefinitionId());

        Map<String, Object> variables = taskService.getVariables(id);
        List<FormField> taskFormData = new ArrayList<>();

        String formLocation = null;
        if (StringUtils.isBlank(taskDto.getFormKey())) {
            taskFormData = formService.getTaskFormData(id).getFormFields();
        } else {
            formLocation = FormUtils.getFormLocation(taskDto.getFormKey(), request);
        }
        return new CustomTaskDto(taskDto, taskFormData, variables, formLocation, processInstance, processDefinition);
    }

}
