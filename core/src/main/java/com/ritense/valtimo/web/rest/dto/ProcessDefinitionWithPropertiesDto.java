/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;

public class ProcessDefinitionWithPropertiesDto extends ProcessDefinitionDto {

    protected boolean isReadOnly;

    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public static ProcessDefinitionWithPropertiesDto fromProcessDefinition(ProcessDefinition definition) {
        ProcessDefinitionWithPropertiesDto dto = new ProcessDefinitionWithPropertiesDto();
        dto.id = definition.getId();
        dto.key = definition.getKey();
        dto.category = definition.getCategory();
        dto.description = definition.getDescription();
        dto.name = definition.getName();
        dto.version = definition.getVersion();
        dto.resource = definition.getResourceName();
        dto.deploymentId = definition.getDeploymentId();
        dto.diagram = definition.getDiagramResourceName();
        dto.suspended = definition.isSuspended();
        dto.tenantId = definition.getTenantId();
        dto.versionTag = definition.getVersionTag();
        dto.historyTimeToLive = definition.getHistoryTimeToLive();
        dto.isStartableInTasklist = definition.isStartableInTasklist();
        return dto;
    }

}
