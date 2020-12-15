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

package com.ritense.valtimo.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class BpmnModelService {

    private final RepositoryService repositoryService;

    public BpmnModelService(final RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public org.camunda.bpm.model.bpmn.instance.Task getTask(Task task) {
        final BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(task.getProcessDefinitionId());
        return bpmnModelInstance.getModelElementById(task.getTaskDefinitionKey());
    }

}
