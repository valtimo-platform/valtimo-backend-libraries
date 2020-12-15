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

package com.ritense.valtimo.web.rest.dto;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CustomTaskDto implements Serializable {

    private TaskDto task;

    private List<FormField> formFields;

    private Map<String, Object> variables;

    private String formLocation;

    private String processName;

    private String processKey;

    private String processVersion;

    private String businessKey;

    public CustomTaskDto() {
    }

    public CustomTaskDto(
            TaskDto task,
            List<FormField> formFields,
            Map<String, Object> variables,
            String formLocation,
            ProcessInstance processInstance,
            ProcessDefinition processDefinition
    ) {
        this.task = task;
        this.formFields = formFields;
        this.variables = variables;
        this.formLocation = formLocation;
        this.businessKey = processInstance.getBusinessKey();
        this.processName = processDefinition.getName();
        this.processKey = processDefinition.getKey();
        this.processVersion = Integer.toString(processDefinition.getVersion());
        this.businessKey = processInstance.getBusinessKey();
    }

    public TaskDto getTask() {
        return task;
    }

    public List<FormField> getFormFields() {
        return formFields;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setTask(TaskDto task) {
        this.task = task;
    }

    public void setFormFields(List<FormField> formFields) {
        this.formFields = formFields;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getFormLocation() {
        return formLocation;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getProcessName() {
        return processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public String getProcessKey() {
        return processKey;
    }
}
