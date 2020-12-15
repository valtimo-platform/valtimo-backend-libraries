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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.valtimo.web.rest.dto.processvariable.ProcessVariableDTOV2;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessInstanceSearchDTO {

    @JsonProperty(value = "processVariables")
    private List<ProcessVariableDTOV2> processVariables;

    public ProcessInstanceSearchDTO() {
    }

    public ProcessInstanceSearchDTO(List<ProcessVariableDTOV2> processVariables) {
        this.processVariables = processVariables;
    }

    public List<ProcessVariableDTOV2> getProcessVariables() {
        return processVariables;
    }
}
