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

package com.ritense.valtimo.web.rest.dto;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import java.util.List;

public class ProcessInstanceDiagramDto {

    private String id;
    private String bpmn20Xml;
    private List<HistoricActivityInstance> historicActivityInstances;

    public ProcessInstanceDiagramDto(String id, String bpmn20Xml, List<HistoricActivityInstance> historicActivityInstances) {
        this.id = id;
        this.bpmn20Xml = bpmn20Xml;
        this.historicActivityInstances = historicActivityInstances;
    }

    public List<HistoricActivityInstance> getHistoricActivityInstances() {
        return historicActivityInstances;
    }

    public void setHistoricActivityInstances(List<HistoricActivityInstance> historicActivityInstances) {
        this.historicActivityInstances = historicActivityInstances;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBpmn20Xml() {
        return bpmn20Xml;
    }

    public void setBpmn20Xml(String bpmn20Xml) {
        this.bpmn20Xml = bpmn20Xml;
    }

    public static ProcessInstanceDiagramDto create(ProcessDefinitionDiagramDto definitionDiagramDto, List<HistoricActivityInstance> historicActivityInstances) {
        return new ProcessInstanceDiagramDto(definitionDiagramDto.getId(), definitionDiagramDto.getBpmn20Xml(), historicActivityInstances);
    }
}
