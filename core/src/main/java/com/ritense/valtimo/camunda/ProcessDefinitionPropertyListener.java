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

package com.ritense.valtimo.camunda;

import com.ritense.valtimo.domain.processdefinition.ProcessDefinitionProperties;
import com.ritense.valtimo.event.ProcessDefinitionDeployedEvent;
import com.ritense.valtimo.processdefinition.repository.ProcessDefinitionPropertiesRepository;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.ModelInstance;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.ByteArrayInputStream;

public class ProcessDefinitionPropertyListener {

    private static final String SYSTEM_PROCESS_PROPERTY = "systemProcess";

    private final ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository;
    private final RepositoryService repositoryService;

    public ProcessDefinitionPropertyListener(
        ProcessDefinitionPropertiesRepository processDefinitionPropertiesRepository,
        RepositoryService repositoryService
    ) {
        this.processDefinitionPropertiesRepository = processDefinitionPropertiesRepository;
        this.repositoryService = repositoryService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        repositoryService.createProcessDefinitionQuery().latestVersion().list().forEach(processDefinition ->
            saveProcessDefinitionProperties(
                processDefinition.getKey(),
                isSystemProcess(repositoryService.getBpmnModelInstance(processDefinition.getId()))
            )
        );
    }

    @EventListener(ProcessDefinitionDeployedEvent.class)
    public void onProcessDefinitionParsedEvent(ProcessDefinitionDeployedEvent event) {
        var processDefinitionResource = event.getDeployment().getResource(event.getProcessDefinition().getResourceName());
        var bpmnModelInstance = Bpmn.readModelFromStream(new ByteArrayInputStream(processDefinitionResource.getBytes()));
        saveProcessDefinitionProperties(event.getProcessDefinition().getKey(), isSystemProcess(bpmnModelInstance));
    }

    private void saveProcessDefinitionProperties(String processDefinitionKey, boolean systemProcess) {
        processDefinitionPropertiesRepository.save(new ProcessDefinitionProperties(processDefinitionKey, systemProcess));
    }

    private boolean isSystemProcess(ModelInstance processModelInstance) {
        final var processProperties = processModelInstance.getModelElementsByType(CamundaProperty.class);
        return processProperties != null && processProperties.stream().anyMatch(this::isSystemProcess);
    }

    private boolean isSystemProcess(CamundaProperty processProperty) {
        return processProperty.getCamundaName().equals(SYSTEM_PROCESS_PROPERTY)
            && Boolean.parseBoolean(processProperty.getCamundaValue());
    }

}
