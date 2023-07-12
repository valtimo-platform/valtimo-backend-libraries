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

import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.dto.CamundaTaskDto;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.service.util.FormUtils;
import com.ritense.valtimo.web.rest.dto.CustomTaskDto;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTaskResource {

    final FormService formService;
    final CamundaTaskService camundaTaskService;
    private final CamundaProcessService camundaProcessService;

    AbstractTaskResource(
        final FormService formService,
        final CamundaTaskService camundaTaskService,
        final CamundaProcessService camundaProcessService
    ) {
        this.formService = formService;
        this.camundaTaskService = camundaTaskService;
        this.camundaProcessService = camundaProcessService;
    }

    public CustomTaskDto createCustomTaskDto(String id, HttpServletRequest request) {
        final CamundaTask task = camundaTaskService.findTaskById(id);
        CamundaTaskDto taskDto = CamundaTaskDto.of(task);

        ProcessInstance processInstance = camundaProcessService.findProcessInstanceById(taskDto.getProcessInstanceId()).orElseThrow();
        CamundaProcessDefinition processDefinition = camundaProcessService.findProcessDefinitionById(processInstance.getProcessDefinitionId());

        Map<String, Object> variables = camundaTaskService.getVariables(id);
        List<FormField> taskFormData = new ArrayList<>();

        String formLocation = null;
        String formKey = formService.getTaskFormKey(taskDto.getProcessDefinitionId(), taskDto.getTaskDefinitionKey());
        if (StringUtils.isBlank(formKey)) {
            taskFormData = formService.getTaskFormData(id).getFormFields();
        } else {
            formLocation = FormUtils.getFormLocation(formKey, request);
        }
        return new CustomTaskDto(taskDto, taskFormData, variables, formLocation, processInstance, processDefinition);
    }

}
