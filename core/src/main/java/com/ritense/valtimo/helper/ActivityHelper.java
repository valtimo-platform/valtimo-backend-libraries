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

package com.ritense.valtimo.helper;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityHelper {

    private final RepositoryService repositoryService;

    public ActivityHelper(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public List<CamundaProperty> getCamundaProperties(HistoricTaskInstanceDto historicTaskInstance, String propertyName) {
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(historicTaskInstance.getProcessDefinitionId());
        Task task = bpmnModelInstance.getModelElementById(historicTaskInstance.getTaskDefinitionKey());
        return getCamundaProperties(task, propertyName);
    }

    public List<CamundaProperty> getCamundaProperties(HistoricTaskInstance historicTaskInstance, String propertyName) {
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(historicTaskInstance.getProcessDefinitionId());
        Task task = bpmnModelInstance.getModelElementById(historicTaskInstance.getTaskDefinitionKey());
        return getCamundaProperties(task, propertyName);
    }

    public List<CamundaProperty> getCamundaProperties(Task taskInstance, String propertyName) {
        ExtensionElements extensionElements = taskInstance.getExtensionElements();
        if (extensionElements != null) {
            List<CamundaProperties> camundaExtensionProperties = extensionElements
                .getElementsQuery()
                .filterByType(CamundaProperties.class)
                .list();

            if (camundaExtensionProperties.size() == 1) {
                return filterProperties(propertyName, camundaExtensionProperties.get(0));
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    public List<CamundaProperty> getCamundaProperties(DelegateTask delegateTask, String propertyName) {
        try {
            CamundaProperties camundaExtensionProperties = delegateTask.getBpmnModelElementInstance()
                .getExtensionElements()
                .getElementsQuery()
                .filterByType(CamundaProperties.class)
                .singleResult();
            return filterProperties(propertyName, camundaExtensionProperties);
        } catch (BpmnModelException ex) {
            throw new IllegalStateException("No extension elements found for this task " + delegateTask.getName());
        }
    }

    public Map<String, Object> getCamundaProperties(ExtensionElements bpmnExtensionElements) {
        Map<String, Object> camundaPropertiesMap = new HashMap<>();

        Collection<CamundaProperty> camundaProperties = bpmnExtensionElements
            .getElementsQuery()
            .filterByType(CamundaProperties.class)
            .singleResult().getCamundaProperties();

        for (CamundaProperty property : camundaProperties) {
            camundaPropertiesMap.put(property.getAttributeValue("name"), property.getCamundaValue());
        }

        return camundaPropertiesMap;
    }

    private List<CamundaProperty> filterProperties(String propertyName, CamundaProperties camundaExtensionProperties) {
        return camundaExtensionProperties.getCamundaProperties().stream()
            .filter(camundaProperty -> camundaProperty.getCamundaName().equalsIgnoreCase(propertyName))
            .collect(Collectors.toList());
    }
}
