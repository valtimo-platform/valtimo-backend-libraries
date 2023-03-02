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

package com.ritense.valtimo.repository.camunda.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcessInstance {
    private String id;
    private String businessKey;
    private Date startTime;
    private Date endTime;
    private String processDefinitionKey;
    private String startUserId;
    private String deleteReason;
    private List<Variable> variables = new ArrayList<>();

    public ProcessInstance() {
    }

    public ProcessInstance(String id, String businessKey, Date startTime, Date endTime, String processDefinitionKey, String startUserId, String deleteReason) {
        this.id = id;
        this.businessKey = businessKey;
        this.startTime = startTime;
        this.endTime = endTime;
        this.processDefinitionKey = processDefinitionKey;
        this.startUserId = startUserId;
        this.deleteReason = deleteReason;
    }

    public String getId() {
        return id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getStartUserId() {
        return startUserId;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProcessInstance that = (ProcessInstance) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
