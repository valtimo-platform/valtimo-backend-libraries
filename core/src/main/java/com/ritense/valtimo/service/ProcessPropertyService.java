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

package com.ritense.valtimo.service;

import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.domain.processdefinition.ProcessDefinitionProperties;
import com.ritense.valtimo.processdefinition.repository.ProcessDefinitionPropertiesRepository;

public class ProcessPropertyService {

    private final ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository;
    private final ValtimoProperties valtimoProperties;
    private final CamundaRepositoryService repositoryService;

    public ProcessPropertyService(
        ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository,
        ValtimoProperties valtimoProperties,
        CamundaRepositoryService repositoryService
    ) {
        this.processDefinitionPropertiesRepository = processDefinitionPropertiesRepository;
        this.valtimoProperties = valtimoProperties;
        this.repositoryService = repositoryService;
    }

    public boolean isSystemProcessById(String processDefinitionId) {
        return isSystemProcess(getProcessDefinitionKeyById(processDefinitionId));
    }

    public boolean isSystemProcess(String processDefinitionKey) {
        final var processProperties = processDefinitionPropertiesRepository.findByProcessDefinitionKey(processDefinitionKey);
        if (processProperties == null) {
            throw new RuntimeException("Failed to find properties for process with key: " + processDefinitionKey);
        }
        return processProperties.isSystemProcess();
    }

    public boolean isReadOnlyById(String processDefinitionId) {
        return isReadOnly(getProcessDefinitionKeyById(processDefinitionId));
    }

    public boolean isReadOnly(String processDefinitionKey) {
        return !valtimoProperties.getProcess().isSystemProcessUpdatable() && isSystemProcess(processDefinitionKey);
    }

    private String getProcessDefinitionKeyById(String processDefinitionId) {
        var processDefinition = repositoryService.findById(processDefinitionId);
        if (processDefinition == null) {
            throw new RuntimeException("Failed to find process definition with id: " + processDefinitionId);
        }
        return processDefinition.getKey();
    }

    public ProcessDefinitionProperties findByProcessDefinitionKey(String processDefinitionKey){
        return processDefinitionPropertiesRepository.findByProcessDefinitionKey(processDefinitionKey);
    }

}
