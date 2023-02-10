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

package com.ritense.valtimo.event;

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ProcessDefinitionDeployedEvent {
    private final String previousProcessDefinitionId;
    private final String processDefinitionId;
    private final String processDefinitionKey;
    private final BpmnModelInstance processDefinitionModelInstance;

    public ProcessDefinitionDeployedEvent(DeploymentEntity deployment, ProcessDefinitionEntity processDefinition) {
        this.previousProcessDefinitionId = processDefinition.getPreviousProcessDefinitionId();
        this.processDefinitionId = processDefinition.getId();
        this.processDefinitionKey = processDefinition.getKey();

        var processDefinitionResource = deployment.getResource(processDefinition.getResourceName());
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(processDefinitionResource.getBytes())) {
            this.processDefinitionModelInstance = Bpmn.readModelFromStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public String getPreviousProcessDefinitionId() {
        return previousProcessDefinitionId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public BpmnModelInstance getProcessDefinitionModelInstance() {
        return processDefinitionModelInstance;
    }
}
