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

import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;

public class ProcessDefinitionDiagramWithPropertyDto {

    private String id;
    private String bpmn20Xml;
    private boolean readOnly;
    private boolean systemProcess;

    public String getId() {
        return id;
    }

    public String getBpmn20Xml() {
        return bpmn20Xml;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isSystemProcess() {
        return systemProcess;
    }

    public ProcessDefinitionDiagramWithPropertyDto(ProcessDefinitionDiagramDto processDefinitionDiagramDto, boolean readOnly, boolean systemProcess) {
        this.id = processDefinitionDiagramDto.getId();
        this.bpmn20Xml = processDefinitionDiagramDto.getBpmn20Xml();
        this.readOnly = readOnly;
        this.systemProcess = systemProcess;
    }

}
