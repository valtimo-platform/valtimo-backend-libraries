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

package com.ritense.valtimo.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.delegate.DelegateTask;
import org.operaton.bpm.engine.history.HistoricTaskInstance;
import org.operaton.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.operaton.bpm.model.bpmn.BpmnModelException;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.operaton.bpm.model.bpmn.instance.ExtensionElements;
import org.operaton.bpm.model.bpmn.instance.Task;
import org.operaton.bpm.model.bpmn.instance.operaton.OperatonProperties;
import org.operaton.bpm.model.bpmn.instance.operaton.OperatonProperty;

public class ActivityHelper {

    private final RepositoryService repositoryService;

    public ActivityHelper(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public List<OperatonProperty> getOperatonProperties(HistoricTaskInstanceDto historicTaskInstance, String propertyName) {
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(historicTaskInstance.getProcessDefinitionId());
        Task task = bpmnModelInstance.getModelElementById(historicTaskInstance.getTaskDefinitionKey());
        return getOperatonProperties(task, propertyName);
    }

    public List<OperatonProperty> getOperatonProperties(HistoricTaskInstance historicTaskInstance, String propertyName) {
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(historicTaskInstance.getProcessDefinitionId());
        Task task = bpmnModelInstance.getModelElementById(historicTaskInstance.getTaskDefinitionKey());
        return getOperatonProperties(task, propertyName);
    }

    public List<OperatonProperty> getOperatonProperties(Task taskInstance, String propertyName) {
        ExtensionElements extensionElements = taskInstance.getExtensionElements();
        if (extensionElements != null) {
            List<OperatonProperties> operatonExtensionProperties = extensionElements
                .getElementsQuery()
                .filterByType(OperatonProperties.class)
                .list();

            if (operatonExtensionProperties.size() == 1) {
                return filterProperties(propertyName, operatonExtensionProperties.get(0));
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    public List<OperatonProperty> getOperatonProperties(DelegateTask delegateTask, String propertyName) {
        try {
            OperatonProperties operatonExtensionProperties = delegateTask.getBpmnModelElementInstance()
                .getExtensionElements()
                .getElementsQuery()
                .filterByType(OperatonProperties.class)
                .singleResult();
            return filterProperties(propertyName, operatonExtensionProperties);
        } catch (BpmnModelException ex) {
            throw new IllegalStateException("No extension elements found for this task " + delegateTask.getName());
        }
    }

    public Map<String, Object> getOperatonProperties(ExtensionElements bpmnExtensionElements) {
        Map<String, Object> operatonPropertiesMap = new HashMap<>();

        Collection<OperatonProperty> operatonProperties = bpmnExtensionElements
            .getElementsQuery()
            .filterByType(OperatonProperties.class)
            .singleResult().getOperatonProperties();

        for (OperatonProperty property : operatonProperties) {
            operatonPropertiesMap.put(property.getAttributeValue("name"), property.getOperatonValue());
        }

        return operatonPropertiesMap;
    }

    private List<OperatonProperty> filterProperties(String propertyName, OperatonProperties operatonExtensionProperties) {
        return operatonExtensionProperties.getOperatonProperties().stream()
            .filter(OperatonProperty -> OperatonProperty.getOperatonName().equalsIgnoreCase(propertyName))
            .collect(Collectors.toList());
    }
}
