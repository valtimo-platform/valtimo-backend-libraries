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

import com.ritense.authorization.AuthorizationContext;
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition;
import com.ritense.valtimo.operaton.domain.OperatonTask;
import com.ritense.valtimo.operaton.dto.OperatonTaskDto;
import com.ritense.valtimo.service.OperatonProcessService;
import com.ritense.valtimo.service.OperatonTaskService;
import com.ritense.valtimo.service.util.FormUtils;
import com.ritense.valtimo.web.rest.dto.CustomTaskDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.operaton.bpm.engine.FormService;
import org.operaton.bpm.engine.form.FormField;
import org.operaton.bpm.engine.runtime.ProcessInstance;

public abstract class AbstractTaskResource {

    final FormService formService;
    final OperatonTaskService operatonTaskService;
    private final OperatonProcessService operatonProcessService;

    AbstractTaskResource(
        final FormService formService,
        final OperatonTaskService operatonTaskService,
        final OperatonProcessService operatonProcessService
    ) {
        this.formService = formService;
        this.operatonTaskService = operatonTaskService;
        this.operatonProcessService = operatonProcessService;
    }

    public CustomTaskDto createCustomTaskDto(String id, HttpServletRequest request) {
        final OperatonTask task = operatonTaskService.findTaskById(id);
        OperatonTaskDto taskDto = OperatonTaskDto.of(task);

        ProcessInstance processInstance = AuthorizationContext
            .runWithoutAuthorization(
                () -> operatonProcessService.findProcessInstanceById(taskDto.getProcessInstanceId()).orElseThrow()
            );
        OperatonProcessDefinition processDefinition = AuthorizationContext
            .runWithoutAuthorization(
                () -> operatonProcessService.findProcessDefinitionById(processInstance.getProcessDefinitionId())
            );

        Map<String, Object> variables = operatonTaskService.getVariables(id);
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
