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

package com.ritense.valtimo.repository.camunda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.ritense.valtimo.camunda.dto.CamundaIdentityLinkDto;
import com.ritense.valtimo.camunda.dto.CamundaTaskDto;

import java.util.List;

public class TaskInstanceWithIdentityLink {

    @JsonProperty
    private String businessKey;

    @JsonUnwrapped
    private final CamundaTaskDto taskDto;

    @JsonProperty
    private boolean external;

    @JsonProperty
    private String processDefinitionKey;

    @JsonProperty
    private final List<CamundaIdentityLinkDto> identityLinks;

    public TaskInstanceWithIdentityLink(
        String businessKey,
        CamundaTaskDto taskDto,
        boolean external,
        String processDefinitionKey,
        List<CamundaIdentityLinkDto> identityLinks
    ) {
        this.businessKey = businessKey;
        this.taskDto = taskDto;
        this.external = external;
        this.processDefinitionKey = processDefinitionKey;
        this.identityLinks = identityLinks;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public CamundaTaskDto getTaskDto() {
        return taskDto;
    }

    public boolean isExternal() {
        return external;
    }

    public List<CamundaIdentityLinkDto> getIdentityLinks() {
        return identityLinks;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }
}