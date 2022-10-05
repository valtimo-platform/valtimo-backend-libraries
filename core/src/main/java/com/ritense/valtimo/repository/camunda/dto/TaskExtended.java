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

package com.ritense.valtimo.repository.camunda.dto;

import com.ritense.valtimo.contract.authentication.model.ValtimoUser;

public class TaskExtended extends org.camunda.bpm.engine.rest.dto.task.TaskDto {

    public final String businessKey;
    public final String processDefinitionKey;

    private ValtimoUser valtimoAssignee;

    public TaskExtended(final String businessKey, final String processDefinitionKey) {
        super();
        this.businessKey = businessKey;
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public ValtimoUser getValtimoAssignee() {
        return valtimoAssignee;
    }

    public void setValtimoAssignee(ValtimoUser user) {
        valtimoAssignee = user;
    }

}
