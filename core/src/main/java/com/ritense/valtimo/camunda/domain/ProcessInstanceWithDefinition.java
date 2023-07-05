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

package com.ritense.valtimo.camunda.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class ProcessInstanceWithDefinition {

    @JsonIgnore
    private final CamundaProcessDefinition processDefinition;

    @JsonProperty("processInstance")
    private final ProcessInstanceDto processInstanceDto;

    public ProcessInstanceWithDefinition(
        final ProcessInstance processInstance,
        final CamundaProcessDefinition processDefinition
    ) {
        this.processInstanceDto = new ProcessInstanceDto(processInstance);
        this.processDefinition = processDefinition;
    }

    public CamundaProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public ProcessInstanceDto getProcessInstanceDto() {
        return processInstanceDto;
    }
}