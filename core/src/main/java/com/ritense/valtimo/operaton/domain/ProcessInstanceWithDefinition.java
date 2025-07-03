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

package com.ritense.valtimo.operaton.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.operaton.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.operaton.bpm.engine.runtime.ProcessInstance;

public class ProcessInstanceWithDefinition {

    @JsonIgnore
    private final OperatonProcessDefinition processDefinition;

    @JsonProperty("processInstance")
    private final ProcessInstanceDto processInstanceDto;

    public ProcessInstanceWithDefinition(
        final ProcessInstance processInstance,
        final OperatonProcessDefinition processDefinition
    ) {
        this.processInstanceDto = new ProcessInstanceDto(processInstance);
        this.processDefinition = processDefinition;
    }

    public OperatonProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public ProcessInstanceDto getProcessInstanceDto() {
        return processInstanceDto;
    }
}