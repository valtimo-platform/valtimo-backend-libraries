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

package com.ritense.valtimo.domain.processdefinition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "process_definition_properties")
public class ProcessDefinitionProperties {

    @Id
    @Column(name = "process_definition_key", nullable = false)
    private String processDefinitionKey;

    @Column(name = "system_process")
    private boolean systemProcess;

    public ProcessDefinitionProperties() {
    }

    public ProcessDefinitionProperties(String processDefinitionKey, boolean systemProcess) {
        this.processDefinitionKey = processDefinitionKey;
        this.systemProcess = systemProcess;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public boolean isSystemProcess() {
        return systemProcess;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public void setSystemProcess(boolean systemProcess) {
        this.systemProcess = systemProcess;
    }
}
